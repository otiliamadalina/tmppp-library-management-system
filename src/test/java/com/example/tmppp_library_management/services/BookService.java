package com.example.tmppp_library_management.services;

import com.example.tmppp_library_management.abstractClasses.LibraryItem;
import com.example.tmppp_library_management.book.Author;
import com.example.tmppp_library_management.book.Book;
import com.example.tmppp_library_management.book.FantasyBook;
import com.example.tmppp_library_management.book.RomanceBook;
import com.example.tmppp_library_management.decorator.*;
import com.example.tmppp_library_management.factories.BookFactory;
import com.example.tmppp_library_management.interfaces.IBorrowable;
import com.example.tmppp_library_management.flyweight.Publisher;
import com.example.tmppp_library_management.flyweight.PublisherFactory;
import com.example.tmppp_library_management.user.MemberType;
import com.example.tmppp_library_management.observer.BookEvent;
import com.example.tmppp_library_management.observer.BookObserver;
import com.example.tmppp_library_management.observer.BookSubject;
import com.example.tmppp_library_management.observer.EventType;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class BookService extends LibraryService implements BookSubject {
    private final BookFactory bookFactory;
    private List<IBorrowable> allBooks;
    private List<BookObserver> observers;

    public BookService(BookFactory bookFactory) {
        this.bookFactory = bookFactory;
        this.allBooks = new ArrayList<>();
        this.observers = new ArrayList<>();
    }

    @Override
    public void attach(BookObserver observer) {
        if (observer != null && !observers.contains(observer)) {
            observers.add(observer);
        }
    }

    @Override
    public void detach(BookObserver observer) {
        observers.remove(observer);
    }

    @Override
    public void notifyObservers(BookEvent event) {
        for (BookObserver observer : observers) {
            observer.update(event);
        }
    }

    private void notifyBookAdded(Book book) {
        notifyObservers(new BookEvent(EventType.BOOK_ADDED, book));
    }

    private void notifyBookUpdated(Book book) {
        notifyObservers(new BookEvent(EventType.BOOK_UPDATED, book));
    }

    @Override
    public LibraryItem createItem(int itemId, String title, int year, int pageCount) {
        Author defaultAuthor = new Author(0, "Unknown Author");
        Publisher defaultPublisher = PublisherFactory.getInstance().getPublisher("Unknown Publisher");
        double defaultPrice = 0.0;
        Book book = bookFactory.createBook(itemId, title, year, pageCount, defaultAuthor, "0000-0000", defaultPublisher, defaultPrice);
        allBooks.add(book);
        notifyBookAdded(book);
        return book;
    }

    public FantasyBook createFantasyBook(int itemId, String title, int year, int pages,
                                         Author author, String isbn, String publisherName, double price) {
        Publisher publisher = PublisherFactory.getInstance().getPublisher(publisherName);
        FantasyBook book = bookFactory.createFantasyBook(itemId, title, year, pages, author, isbn, publisher, price);
        allBooks.add(book);
        addItem(book);
        notifyBookAdded(book);
        return book;
    }

    public RomanceBook createRomanceBook(int itemId, String title, int year, int pages,
                                         Author author, String isbn, String publisherName,
                                         int level, String tropes, double price) {
        Publisher publisher = PublisherFactory.getInstance().getPublisher(publisherName);
        RomanceBook book = bookFactory.createRomanceBook(itemId, title, year, pages, author, isbn, publisher, level, tropes, price);
        allBooks.add(book);
        addItem(book);
        notifyBookAdded(book);
        return book;
    }

    public IBorrowable addReadingRoomRestriction(Book book, String room) {
        ReadingRoomDecorator decorator = new ReadingRoomDecorator(book, room);
        allBooks.remove(book);
        allBooks.add(decorator);
        notifyBookUpdated(book);
        return decorator;
    }

    public IBorrowable addRestrictedAccess(Book book, MemberType requiredLevel) {
        RestrictedAccessDecorator decorator = new RestrictedAccessDecorator(book, requiredLevel);
        allBooks.remove(book);
        allBooks.add(decorator);
        notifyBookUpdated(book);
        return decorator;
    }

    public IBorrowable addApprovalRequired(Book book) {
        ApprovalRequiredDecorator decorator = new ApprovalRequiredDecorator(book);
        allBooks.remove(book);
        allBooks.add(decorator);
        notifyBookUpdated(book);
        return decorator;
    }

    public IBorrowable addFullRestrictions(Book book, String room) {
        BookDecorator decorator = new ReadingRoomDecorator(book, room);
        decorator = new RestrictedAccessDecorator(decorator.getOriginalBook(), MemberType.PROFESSOR);
        decorator = new ApprovalRequiredDecorator(decorator.getOriginalBook());
        allBooks.remove(book);
        allBooks.add(decorator);
        notifyBookUpdated(book);
        return decorator;
    }

    public List<IBorrowable> findAllBooks() {
        return new ArrayList<>(allBooks);
    }

    public List<Book> findAllSimpleBooks() {
        return allBooks.stream()
                .filter(item -> item instanceof Book && !(item instanceof BookDecorator))
                .map(item -> (Book) item)
                .collect(Collectors.toList());
    }

    public List<IBorrowable> findBooksByAuthor(String authorName) {
        return allBooks.stream()
                .filter(item -> {
                    Book book = getBookFromItem(item);
                    return book != null && book.getAuthor().getName().toLowerCase().contains(authorName.toLowerCase());
                })
                .collect(Collectors.toList());
    }

    public List<IBorrowable> findBooksByTitle(String title) {
        return allBooks.stream()
                .filter(item -> {
                    Book book = getBookFromItem(item);
                    return book != null && book.getTitle().toLowerCase().contains(title.toLowerCase());
                })
                .collect(Collectors.toList());
    }

    private Book getBookFromItem(IBorrowable item) {
        if (item instanceof Book) {
            return (Book) item;
        } else if (item instanceof BookDecorator) {
            return ((BookDecorator) item).getOriginalBook();
        }
        return null;
    }

    public List<Book> findAllBooksLegacy() {
        return allBooks.stream()
                .filter(item -> item instanceof Book && !(item instanceof BookDecorator))
                .map(item -> (Book) item)
                .collect(Collectors.toList());
    }

    public List<Book> findBooksByAuthorLegacy(String authorName) {
        return findAllBooksLegacy().stream()
                .filter(book -> book.getAuthor().getName().toLowerCase().contains(authorName.toLowerCase()))
                .collect(Collectors.toList());
    }

    public List<Book> findBooksByTitleLegacy(String title) {
        return findAllBooksLegacy().stream()
                .filter(book -> book.getTitle().toLowerCase().contains(title.toLowerCase()))
                .collect(Collectors.toList());
    }

    public void updateBook(int itemId, IBorrowable updatedBook) {
        for (int i = 0; i < allBooks.size(); i++) {
            IBorrowable item = allBooks.get(i);
            Book book = null;
            if (item instanceof Book) {
                book = (Book) item;
            } else if (item instanceof BookDecorator) {
                book = ((BookDecorator) item).getOriginalBook();
            }
            if (book != null && book.getItemId() == itemId) {
                allBooks.set(i, updatedBook);
                notifyBookUpdated(book);
                break;
            }
        }
    }
    public boolean deleteBook(int itemId) {
        for (int i = 0; i < allBooks.size(); i++) {
            IBorrowable item = allBooks.get(i);
            Book book = getBookFromItem(item);
            if (book != null && book.getItemId() == itemId) {
                String isbn = book.getIsbn();
                allBooks.remove(i);
                removeItem(itemId);
                StockService.getInstance().removeStock(isbn);
                notifyObservers(new BookEvent(EventType.BOOK_REMOVED, book));
                return true;
            }
        }
        return false;
    }

    public boolean deleteBookByIsbn(String isbn) {
        for (int i = 0; i < allBooks.size(); i++) {
            IBorrowable item = allBooks.get(i);
            Book book = getBookFromItem(item);
            if (book != null && book.getIsbn().equals(isbn)) {
                int itemId = book.getItemId();
                allBooks.remove(i);
                removeItem(itemId);
                StockService.getInstance().removeStock(isbn);
                notifyObservers(new BookEvent(EventType.BOOK_REMOVED, book));
                return true;
            }
        }
        return false;
    }
}
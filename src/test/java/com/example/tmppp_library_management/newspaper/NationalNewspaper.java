package com.example.tmppp_library_management.newspaper;

import com.example.tmppp_library_management.flyweight.Publisher;

public class NationalNewspaper extends Newspaper {
    private String distributionArea;
    private String politicalOrientation;

    public NationalNewspaper(int itemId, String title, int publicationDate, int pageCount,
                             Publisher publisher, String issn,
                             String distributionArea, String politicalOrientation) {
        super(itemId, title, publicationDate, pageCount, publisher, issn);
        this.distributionArea = distributionArea;
        this.politicalOrientation = politicalOrientation;
    }

    public String getDistributionArea() {
        return distributionArea;
    }

    public void setDistributionArea(String area) {
        this.distributionArea = area;
    }

    public String getPoliticalOrientation() {
        return politicalOrientation;
    }

    public void setPoliticalOrientation(String orientation) {
        this.politicalOrientation = orientation;
    }

    @Override
    public String getDescription() {
        return "NationalNewspaper: " + getTitle() + " - " + distributionArea;
    }
}
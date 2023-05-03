package it.polimi.webapp;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class AuctionList {
    private String name, description, image;
    private Integer codArticle, idAsta;

    private double price;

    public AuctionList(int idAsta, int codArticle, String name, String description, String image, double price) {
        this.name = name;
        this.description = description;
        this.codArticle = codArticle;
        this.image = image;
        this.price = price;
        this.idAsta = idAsta;
    }

    public AuctionList() {
        this.name = "";
        this.description = "";
        this.codArticle = -1;
        this.image = "";
        this.price = -1.0;
        this.idAsta = -1;
    }

    public List<AuctionList> toAuctionList(ResultSet res) throws SQLException {
        List<AuctionList> result = new ArrayList<>();
        while (res.next()) {
            result.add(new AuctionList(res.getInt(1), res.getInt(2),
                    res.getString(3), res.getString(4), res.getString(5), res.getDouble(6)));
        }
        return result;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public Integer getCodArticle() {
        return codArticle;
    }

    public String getImage() {
        return image;
    }

    public Integer getIdAsta() {
        return idAsta;
    }

    public double getPrice() {
        return price;
    }
}
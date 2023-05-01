package it.polimi.webapp;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ArticleObjectsList {
    private String name, description;
    private Integer codArticle;

    public ArticleObjectsList(int codArticle, String name, String description) {
        this.name = name;
        this.description = description;
        this.codArticle = codArticle;
    }

    public ArticleObjectsList() {
        this.name = "";
        this.description = "";
        this.codArticle = -1;
    }

    public List<ArticleObjectsList> toArticleObjectsList(ResultSet res) throws SQLException {
        List<ArticleObjectsList> result = new ArrayList<>();
        while (res.next()) {
            result.add(new ArticleObjectsList(res.getInt(1), res.getString(2), res.getString(3)));
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
}

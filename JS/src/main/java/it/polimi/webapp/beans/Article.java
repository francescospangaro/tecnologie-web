package it.polimi.webapp.beans;

import org.jetbrains.annotations.Nullable;

public record Article(int codArticle,
                      String name,
                      String description,
                      String immagine,
                      double prezzo,
                      @Nullable Integer idUtente) {

    public Article(int codArticle,
                   String name,
                   String description,
                   String immagine,
                   double prezzo) {
        this(codArticle, name, description, immagine, prezzo, null);
    }
}

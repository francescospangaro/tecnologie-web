package it.polimi.webapp.beans;

import org.jetbrains.annotations.Nullable;

public record Article(int codArticle,
                      @Nullable String name,
                      @Nullable String description,
                      @Nullable String immagine,
                      double prezzo,
                      @Nullable Integer idUtente) {

    public Article(String name,
                   String description,
                   String immagine,
                   double prezzo,
                   int idUtente) {
        this(-1, name, description, immagine, prezzo, idUtente);
    }

    public Article(int codArticle,
                   String name,
                   String description,
                   String immagine,
                   double prezzo) {
        this(codArticle, name, description, immagine, prezzo, null);
    }

    public Article(int codArticle) {
        this(codArticle, null, null, null, -1, null);
    }

    @Override
    public int codArticle() {
        if(codArticle == -1)
            throw new IllegalStateException("Article was created without id");
        return codArticle;
    }

    @Override
    public String name() {
        if(name == null)
            throw new IllegalStateException("Article was created without name");
        return name;
    }

    @Override
    public String description() {
        if(description == null)
            throw new IllegalStateException("Article was created without description");
        return description;
    }

    @Override
    public double prezzo() {
        if(prezzo == -1D)
            throw new IllegalStateException("Article was created without prezzo");
        return prezzo;
    }
}

package it.polimi.webapp.beans;

public record Article(int codArticle,
                      String name,
                      String description,
                      String immagine,
                      double prezzo,
                      int idUtente) {

    public Article(int codArticle,
                   String name,
                   String description,
                   String immagine,
                   double prezzo) {
        this(codArticle, name, description, immagine, prezzo, -1);
    }

    @Override
    public int idUtente() {
        if(idUtente == -1)
            throw new IllegalStateException("Article was created without idUtente");
        return idUtente;
    }
}

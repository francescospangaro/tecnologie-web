package it.polimi.webapp;

import it.polimi.webapp.beans.Article;
import it.polimi.webapp.beans.Auction;
import it.polimi.webapp.beans.Offer;
import it.polimi.webapp.dao.ArticleDao;
import it.polimi.webapp.dao.AuctionDao;
import it.polimi.webapp.dao.OffersDao;
import net.datafaker.Faker;
import org.glassfish.soteria.identitystores.hash.Pbkdf2PasswordHashImpl;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import javax.net.ssl.HttpsURLConnection;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.text.NumberFormat;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.random.RandomGenerator;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertFalse;

public class FillDatabaseTest {

    @Test
    @Disabled // Uncomment to run
    @SuppressWarnings({"NullAway", "StatementWithEmptyBody"}) // Bad code, just for testing
    void doFill() throws Exception {

        var locale = Locale.ITALIAN;
        var numFormat = NumberFormat.getInstance(locale);
        var faker = new Faker(locale);
        var rnd = RandomGenerator.getDefault();
        var pswd = new Pbkdf2PasswordHashImpl();

        try (Connection conn = DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/casa_daste?serverTimezone=UTC",
                "root",
                "Francesc0")) {
            Transactions.startNullable(conn, Transactions.Type.NEW_REQUIRED, tx -> {
                try (var stmt = tx.createStatement()) {
                    stmt.executeUpdate("DELETE FROM offerta WHERE 1");
                }

                try (var stmt = tx.createStatement()) {
                    stmt.executeUpdate("DELETE FROM astearticoli WHERE 1");
                }

                try (var stmt = tx.createStatement()) {
                    stmt.executeUpdate("DELETE FROM articolo WHERE 1");
                }

                try (var stmt = tx.createStatement()) {
                    stmt.executeUpdate("DELETE FROM asta WHERE 1");
                }

                try (var stmt = tx.createStatement()) {
                    stmt.executeUpdate("DELETE FROM utente WHERE 1");
                }

                var userIds = new ArrayList<Integer>();
                System.out.println("Generating users...");
                try (var statement = tx.prepareStatement("""
                                INSERT INTO utente(nome, cognome, email, password, indirizzo)
                                VALUES (?, ?, ?, ?, ?)
                                """,
                        Statement.RETURN_GENERATED_KEYS)) {
                    for (int i = 0, len = rnd.nextInt(20, 40); i < len; i++) {
                        statement.setString(1, faker.name().firstName());
                        statement.setString(2, faker.name().lastName());
                        statement.setString(3, faker.internet().emailAddress());
                        statement.setString(4, pswd.generate("password".toCharArray()));
                        statement.setString(5, faker.address().streetAddress());
                        statement.addBatch();
                    }

                    statement.executeBatch();

                    try (var generatedKeys = statement.getGeneratedKeys()) {
                        while (generatedKeys.next())
                            userIds.add(generatedKeys.getInt(1));
                    }
                }

                System.out.println("Generating images...");
                var generatedImages = new ArrayList<byte[]>();
                for (int i = 0, len = 20; i < len; i++) {
                    System.out.println("Generating image " + i + "/" + len + "...");
                    HttpsURLConnection httpConn = (HttpsURLConnection) new URL(faker.internet().image()).openConnection();
                    try (InputStream is = httpConn.getInputStream()) {
                        generatedImages.add(is.readAllBytes());
                    }
                }

                System.out.println("Generating articles...");
                var articleDao = new ArticleDao(tx);
                var usersAndArticles = new HashMap<Integer, List<Integer>>();
                for (Integer userId : userIds) {
                    var generatedArticles = new ArrayList<Integer>();
                    for (int i = 0, len = rnd.nextInt(0, 20); i < len; i++) {
                        try (InputStream is = new ByteArrayInputStream(generatedImages.get(rnd.nextInt(generatedImages.size())))) {
                            generatedArticles.add(articleDao.insertArticle(new Article(
                                    faker.commerce().productName(),
                                    faker.lorem().sentence(5, 4),
                                    "",
                                    numFormat.parse(faker.commerce().price(10, 100)).doubleValue(),
                                    userId
                            ), is));
                        }
                    }

                    usersAndArticles.put(userId, generatedArticles);
                }

                var loginTime = LocalDateTime.now();

                System.out.println("Generating auctions...");
                var auctionDao = new AuctionDao(tx);
                var allExpired = new ArrayList<Integer>();
                var allNonExpired = new ArrayList<Integer>();
                var auctionToUser = new HashMap<Integer, Integer>();
                var auctionToExpiration = new HashMap<Integer, LocalDateTime>();
                for (Integer userId : userIds) {
                    var articles = usersAndArticles.get(userId);
                    if (articles.isEmpty())
                        continue;

                    var generatedNonExpiredAuctions = new ArrayList<Integer>();
                    var generatedExpiredAuctions = new ArrayList<Integer>();
                    for (int i = 0, len = rnd.nextInt(5, 20); i < len; i++) {
                        boolean expired = rnd.nextBoolean();
                        var expiry = expired
                                ? loginTime.minus(rnd.nextInt(100), ChronoUnit.DAYS)
                                : loginTime.plus(rnd.nextInt(100), ChronoUnit.DAYS);
                        var auction = auctionDao.insertAuction(new Auction(
                                expiry,
                                IntStream.range(0, rnd.nextInt(1, 20))
                                        .map(ignored -> articles.get(rnd.nextInt(articles.size())))
                                        .distinct()
                                        .mapToObj(Article::new)
                                        .toList(),
                                numFormat.parse(faker.commerce().price(1, 10)).intValue()));

                        var list = expired ? generatedExpiredAuctions : generatedNonExpiredAuctions;
                        list.add(auction);
                        auctionToExpiration.put(auction, expiry);
                        auctionToUser.put(auction, userId);
                    }

                    allExpired.addAll(generatedExpiredAuctions);
                    allNonExpired.addAll(generatedNonExpiredAuctions);
                }

                var allAuctions = Stream.concat(allExpired.stream(), allNonExpired.stream()).toList();
                assertFalse(allAuctions.isEmpty());

                System.out.println("Generating offers...");
                // Force a fake clock to make sure we can insert offers on expired auctions
                var offersDao = new OffersDao(tx, Clock.fixed(Instant.ofEpochMilli(0), ZoneId.systemDefault()));
                for (int auction : allAuctions) {
                    var minOffers = allExpired.contains(auction) ? 2 : 0;

                    var numOffers = rnd.nextInt(minOffers, 10);
                    var maxBound = rnd.nextInt(numOffers, 100);
                    var startDate = auctionToExpiration.get(auction).minus(maxBound, ChronoUnit.DAYS);

                    for (int i = 0; i < numOffers; i++) {
                        while (offersDao.insertOffer(new Offer(
                                userIds.get(rnd.nextInt(userIds.size())),
                                auction,
                                numFormat.parse(faker.commerce().price(1000, 100000D / numOffers * i)).doubleValue(),
                                i == 0 ? startDate
                                        : startDate.plusDays(rnd.nextInt(
                                        maxBound / numOffers * (i - 1),
                                        maxBound / numOffers * i))
                        )).type() != OffersDao.TypeError.DONE) ;
                    }
                }

                System.out.println("Closing auctions...");
                var toClose = IntStream.range(0, allExpired.size())
                        .map(ignored -> allExpired.get(rnd.nextInt(allExpired.size())))
                        .distinct()
                        .boxed()
                        .toList();
                for (int auctionToClose : toClose)
                    auctionDao.closeAuction(auctionToClose, auctionToUser.get(auctionToClose));
                return null;
            });
        }
    }
}

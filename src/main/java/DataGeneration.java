import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.joda.time.LocalDate;

import com.google.common.base.Charsets;
import com.google.common.collect.Lists;
import com.google.common.io.Files;

public class DataGeneration {

    private static final int MAX_SCORE = 100;

    public static void main(String[] args) throws IOException {
        List<String> games = readIn("games");
        List<String> names = readIn("names");
        List<String> locations = readIn("locations");
        List<String> domains = readIn("domains");

        Map<String, String> accounts = new HashMap<String, String>();
        for (String name : names) {
            String email = String.format("%s@%s", name, pickIn(domains));
            String json = String.format("{ \"email\" : \"%s\", \"location\" : [%s] }", email, pickIn(locations));
            accounts.put(name, json);
            System.out.print(".");
        }
        System.out.println();
        System.out.println("Accounts OK");

        dumpIn("data.json", accounts.values());

        LocalDate from = new LocalDate(2014, 1, 1);
        LocalDate to = new LocalDate(2014, 12, 1);

        int id = 1;
        Random random = new Random();
        List<String> plays = new ArrayList<String>();
        for (LocalDate currentDate = from; currentDate.isBefore(to); currentDate = currentDate.plusDays(1)) {
            for (String game : games) {
                List<String> accountNames = Lists.newArrayList(accounts.keySet());

                for (int firstPlayerId = 0; firstPlayerId < accountNames.size(); firstPlayerId++) {
                    for (int secondPlayerId = 0; secondPlayerId < accountNames.size(); secondPlayerId++) {
                        if (firstPlayerId == secondPlayerId) {
                            continue;
                        }

                        String firstPlayerName = accountNames.get(firstPlayerId);
                        int firstPlayerScore = random.nextInt(MAX_SCORE);

                        String secondPlayerName = accountNames.get(secondPlayerId);
                        int secondPlayerScore = random.nextInt(MAX_SCORE);

                        String players = String.format("{ \"name\" : \"%s\", \"account\" : %s }, { \"name\" : \"%s\", \"account\" : %s }",
                                formatName(firstPlayerName, game), accounts.get(firstPlayerName), formatName(secondPlayerName, game),
                                accounts.get(secondPlayerName));

                        String scores = String.format("{ \"name\" : \"%s\", \"score\" : %d }, { \"name\" : \"%s\", \"score\" : %d }",
                                formatName(firstPlayerName, game), firstPlayerScore, formatName(secondPlayerName, game), secondPlayerScore);

                        String winner = firstPlayerScore > secondPlayerScore ? formatName(firstPlayerName, game)
                                : firstPlayerScore < secondPlayerScore ? formatName(secondPlayerName, game) : "draw";

                        String json = String.format(
                                "{ \"id\" : \"%d\", \"game\" : { \"name\" : \"%s\" }, \"date\" : \"%s\", \"players\" : [ %s ], \"scores\" : [ %s ], \"winner\" : \"%s\" }", id,
                                game, currentDate, players, scores, winner);
                        plays.add(json);

                        id++;
                        System.out.print(".");
                        if (id % 1000 == 0) {
                            System.out.println();
                        }
                    }
                }
                System.out.println();
                System.out.println(game + " OK");
            }
            System.out.println();
            System.out.println(currentDate + " OK");

            appendIn("data.json", plays);
            plays.clear();
        }
    }

    private static String formatName(String accountName, String gameName) {
        return accountName + "-" + gameName.toLowerCase().replaceAll("\\s", "");
    }

    private static void dumpIn(String fileName, Collection<String> objects) throws IOException {
        File file = new File(fileName);
        if (file.exists()) {
            file.delete();
        }
        appendIn(fileName, objects);
    }

    private static void appendIn(String fileName, Collection<String> objects) throws IOException {
        File file = new File(fileName);
        for (String object : objects) {
            Files.append(object, file, Charsets.UTF_8);
            Files.append("\n", file, Charsets.UTF_8);
        }
    }

    private static List<String> readIn(String name) throws IOException {
        return Files.readLines(new File("./src/main/resources/" + name + ".txt"), Charsets.UTF_8);
    }

    private static <T> T pickIn(List<T> objects) {
        return objects.get(new Random().nextInt(objects.size()));
    }
    /*
     * { "numero" : 1, "jeu" : { "nom" : "perudo", } "joueurs" : [{ "nom" :
     * "gringo", "compte" : { "email" : "gringo@dojo.serli.com", "location" :
     * [0.0 0.0] }, { "nom" : "pepito", "compte" : { "email" :
     * "pepito@dojo.serli.com", "location" : [0.0 0.0] }, { "nom" : "speedy",
     * "compte" : { "email" : "speedy@dojo.serli.com", "location" : [0.0 0.0] }]
     * ] "score" : [{ "joueur" : "gringo", "score" : 5 }, { "joueur" : "pepito",
     * "score" : 3 }, { "joueur" : "speedy", "score" : 4 }] "vainqueur" :
     * "gringo" }
     */
}

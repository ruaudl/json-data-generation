import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;

import com.google.common.base.Charsets;
import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;
import com.google.common.io.Files;
import com.google.common.primitives.Ints;

public class DataGeneration {

	private static final int MAX_SCORE = 100;
	private static final int LINES_NUMBER = 10000;
	private static final String INDEX = "engine";
	private static final String TYPE = "match";

	public static void main(String[] args) throws IOException {
		List<Game> games = readGamesIn("games");
		List<Account> accounts = readAccountsIn("names", "domains", "locations");

		int id = 1;
		Random random = new Random();
		List<String> plays = new ArrayList<String>();

		String targetJson = String.format(
				"{ \"index\" : { \"_index\" : \"%s\", \"_type\" : \"%s\" } }",
				INDEX, TYPE);

		for (int i = 0; i < LINES_NUMBER; i++) {
			final Game game = pickIn(games);

			int minPlayers = game.minPlayers != null ? game.minPlayers : 1;
			int maxPlayers = game.maxPlayers != null ? game.maxPlayers
					: accounts.size();
			int playersCount = Math.min(
					random.nextInt(maxPlayers) + minPlayers, accounts.size());

			Set<Account> players = new HashSet<Account>();

			while (players.size() < playersCount) {
				players.add(pickIn(accounts));
			}

			String playersJson = Joiner.on(", ").join(
					Collections2.transform(players,
							new Function<Account, String>() {
								public String apply(Account account) {
									return String
											.format("{ \"name\" : \"%s\", \"account\" : %s }",
													formatName(account, game),
													account);
								}
							}));

			Map<Integer, Account> scores = new HashMap<Integer, Account>();
			for (Account account : players) {
				scores.put(random.nextInt(MAX_SCORE), account);
			}

			String scoresJson = Joiner
					.on(", ")
					.join(Collections2.transform(
							scores.entrySet(),
							new Function<Map.Entry<Integer, Account>, String>() {
								public String apply(
										Map.Entry<Integer, Account> entry) {
									return String
											.format("{ \"name\" : \"%s\", \"score\" : %d }",
													formatName(
															entry.getValue(),
															game), entry
															.getKey());
								}
							}));

			String winner = "";
			// firstPlayerScore > secondPlayerScore ? formatName(
			// firstPlayer, game)
			// : firstPlayerScore < secondPlayerScore ? formatName(
			// secondPlayer, game) : "draw";

			LocalDateTime startTime = pickDateTime();
			LocalDateTime endTime = pickDateTime(startTime, 6);

			String json = String
					.format("%s\n{ \"id\" : \"%d\", \"game\" : %s, \"startTime\" : \"%s\", \"endTime\" : \"%s\", \"players\" : [ %s ], \"scores\" : [ %s ], \"winner\" : \"%s\" }",
							targetJson, id, game, startTime, endTime,
							playersJson, scoresJson, winner);

			plays.add(json);

			id++;
			System.out.print(".");
			if (id % 1000 == 0) {
				System.out.println();
			}
		}
		dumpIn("data.json", plays);
		plays.clear();
	}

	private static String formatName(Account account, Game game) {
		return account.name + "-"
				+ game.name.toLowerCase().replaceAll("\\s", "");
	}

	private static void dumpIn(String fileName, Collection<String> objects)
			throws IOException {
		File file = new File(fileName);
		if (file.exists()) {
			file.delete();
		}
		appendIn(fileName, objects);
	}

	private static void appendIn(String fileName, Collection<String> objects)
			throws IOException {
		File file = new File(fileName);
		for (String object : objects) {
			Files.append(object, file, Charsets.UTF_8);
			Files.append("\n", file, Charsets.UTF_8);
		}
	}

	private static List<String> readIn(String name) throws IOException {
		return Files.readLines(
				new File("./src/main/resources/" + name + ".txt"),
				Charsets.UTF_8);
	}

	private static List<Game> readGamesIn(String gamesFile) throws IOException {

		List<String> gameLines = readIn(gamesFile);

		List<Game> games = new ArrayList<Game>(gameLines.size());
		for (String line : gameLines) {
			List<String> strings = Lists.newArrayList(Splitter.on('|').split(
					line));

			Game game = new Game(strings.get(0), Ints.tryParse(strings.get(1)),
					Ints.tryParse(strings.get(2)), strings.get(3));
			games.add(game);
		}

		return games;
	}

	private static List<Account> readAccountsIn(String namesFile,
			String domainsFile, String locationsFile) throws IOException {

		List<String> names = readIn(namesFile);
		List<String> domains = readIn(domainsFile);
		List<String> locations = readIn(locationsFile);

		List<Account> accounts = new ArrayList<Account>();
		for (String name : names) {
			String email = String.format("%s@%s", name, pickIn(domains));
			accounts.add(new Account(name, email, pickIn(locations),
					new LocalDate()));
		}
		return accounts;
	}

	private static <T> T pickIn(List<T> objects) {
		return objects.get(new Random().nextInt(objects.size()));
	}

	private static LocalDateTime pickDateTime() {
		return pickDateTime(new LocalDateTime(2014, 1, 1, 0, 0), 330 * 24 * 60);
	}

	private static LocalDateTime pickDateTime(LocalDateTime from,
			int minutesRange) {
		return from.plusMinutes(new Random().nextInt(minutesRange));
	}

	public static class Account {
		String name, email, locations;
		LocalDate subscription;

		public Account(String name, String email, String locations,
				LocalDate subscription) {
			super();
			this.name = name;
			this.email = email;
			this.locations = locations;
			this.subscription = subscription;
		}

		@Override
		public String toString() {
			return String
					.format("{ \"email\" : \"%s\", \"location\" : [%s], \"subscription\" : \"%s\" }",
							email, locations, subscription);
		}
	}

	public static class Game {
		String name, description;
		Integer minPlayers, maxPlayers;

		public Game(String name, Integer minPlayers, Integer maxPlayers,
				String description) {
			super();
			this.name = name;
			this.minPlayers = minPlayers;
			this.maxPlayers = maxPlayers;
			this.description = description;
		}

		@Override
		public String toString() {
			StringBuilder builder = new StringBuilder();
			builder.append("{ \"name\" : \"").append(name).append("\"");
			if (minPlayers != null) {
				builder.append(", \"minPlayers\" : ").append(minPlayers);
			}
			if (maxPlayers != null) {
				builder.append(", \"maxPlayers\" : ").append(maxPlayers);
			}
			if (description != null && !description.isEmpty()) {
				builder.append(", \"description\" : \"").append(description)
						.append("\"");
			}
			builder.append(" }");
			return builder.toString();
		}
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

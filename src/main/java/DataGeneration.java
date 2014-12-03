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
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.google.common.io.Files;
import com.google.common.primitives.Ints;

public class DataGeneration {

	private static final int MAX_SCORE = 100;
	private static final int LINES_NUMBER = 10000;

	public static void main(String[] args) throws IOException {
		List<Game> games = readGamesIn("games");
		List<Account> accounts = readAccountsIn("names", "domains", "locations");

		LocalDate from = new LocalDate(2014, 1, 1);
		LocalDate to = new LocalDate(2014, 12, 1);

		int id = 1;
		Random random = new Random();
		List<String> plays = new ArrayList<String>();

		for (int i = 0; i < LINES_NUMBER; i++) {
			LocalDate currentDate = getDate(from, random);

			Game game = getRandom(random, games);

			Account firstPlayer = pickIn(accounts);
			Account secondPlayer = firstPlayer;
			while (secondPlayer == firstPlayer) {
				secondPlayer = pickIn(accounts);
			}

			int firstPlayerScore = random.nextInt(MAX_SCORE);
			int secondPlayerScore = random.nextInt(MAX_SCORE);

			String players = String
					.format("{ \"name\" : \"%s\", \"account\" : %s }, { \"name\" : \"%s\", \"account\" : %s }",
							formatName(firstPlayer, game), firstPlayer,
							formatName(secondPlayer, game), secondPlayer);

			String scores = String
					.format("{ \"name\" : \"%s\", \"score\" : %d }, { \"name\" : \"%s\", \"score\" : %d }",
							formatName(firstPlayer, game), firstPlayerScore,
							formatName(secondPlayer, game), secondPlayerScore);

			String winner = firstPlayerScore > secondPlayerScore ? formatName(
					firstPlayer, game)
					: firstPlayerScore < secondPlayerScore ? formatName(
							secondPlayer, game) : "draw";

			String json = String
					.format("{ \"id\" : \"%d\", \"game\" : %s, \"date\" : \"%s\", \"players\" : [ %s ], \"scores\" : [ %s ], \"winner\" : \"%s\" }",
							id, game, currentDate, players, scores, winner);
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

	private static <T> T getRandom(Random random, Collection<T> list) {
		int index = random.nextInt(list.size());
		return Lists.newArrayList(list).get(index);
	}

	private static LocalDate getDate(LocalDate from, Random random) {
		return from.plusDays(random.nextInt(330));
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

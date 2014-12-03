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
		Map<String, Game> games = readGamesIn("games");
		List<String> names = readIn("names");
		List<String> locations = readIn("locations");
		List<String> domains = readIn("domains");

		Map<String, String> accounts = new HashMap<String, String>();
		for (String name : names) {
			String email = String.format("%s@%s", name, pickIn(domains));
			String json = String.format(
					"{ \"email\" : \"%s\", \"location\" : [%s] }", email,
					pickIn(locations));
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

		List<String> accountNames = Lists.newArrayList(accounts.keySet());
		for (int i = 0; i < LINES_NUMBER; i++) {
			LocalDate currentDate = getDate(from, random);

			Game game = getRandom(random, games.values());

			int firstPlayerId = getPlayerId(random, accounts.size());
			int secondPlayerId = -1;
			do {
				secondPlayerId = getPlayerId(random, accounts.size());
			} while (secondPlayerId == firstPlayerId);
			String firstPlayerName = accountNames.get(firstPlayerId);
			int firstPlayerScore = random.nextInt(MAX_SCORE);

			String secondPlayerName = accountNames.get(secondPlayerId);
			int secondPlayerScore = random.nextInt(MAX_SCORE);

			String players = String
					.format("{ \"name\" : \"%s\", \"account\" : %s }, { \"name\" : \"%s\", \"account\" : %s }",
							formatName(firstPlayerName, game),
							accounts.get(firstPlayerName),
							formatName(secondPlayerName, game),
							accounts.get(secondPlayerName));

			String scores = String
					.format("{ \"name\" : \"%s\", \"score\" : %d }, { \"name\" : \"%s\", \"score\" : %d }",
							formatName(firstPlayerName, game),
							firstPlayerScore,
							formatName(secondPlayerName, game),
							secondPlayerScore);

			String winner = firstPlayerScore > secondPlayerScore ? formatName(
					firstPlayerName, game)
					: firstPlayerScore < secondPlayerScore ? formatName(
							secondPlayerName, game) : "draw";

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
		appendIn("data.json", plays);
		plays.clear();

		// for (LocalDate currentDate = from; currentDate.isBefore(to);
		// currentDate = currentDate.plusDays(1)) {
		// for (String game : games) {
		//
		// for (int firstPlayerId = 0; firstPlayerId < accountNames.size();
		// firstPlayerId++) {
		// for (int secondPlayerId = 0; secondPlayerId < accountNames.size();
		// secondPlayerId++) {
		// if (firstPlayerId == secondPlayerId) {
		// continue;
		// }
		//
		// String firstPlayerName = accountNames.get(firstPlayerId);
		// int firstPlayerScore = random.nextInt(MAX_SCORE);
		//
		// String secondPlayerName = accountNames.get(secondPlayerId);
		// int secondPlayerScore = random.nextInt(MAX_SCORE);
		//
		// String players =
		// String.format("{ \"name\" : \"%s\", \"account\" : %s }, { \"name\" : \"%s\", \"account\" : %s }",
		// formatName(firstPlayerName, game), accounts.get(firstPlayerName),
		// formatName(secondPlayerName, game),
		// accounts.get(secondPlayerName));
		//
		// String scores =
		// String.format("{ \"name\" : \"%s\", \"score\" : %d }, { \"name\" : \"%s\", \"score\" : %d }",
		// formatName(firstPlayerName, game), firstPlayerScore,
		// formatName(secondPlayerName, game), secondPlayerScore);
		//
		// String winner = firstPlayerScore > secondPlayerScore ?
		// formatName(firstPlayerName, game)
		// : firstPlayerScore < secondPlayerScore ? formatName(secondPlayerName,
		// game) : "draw";
		//
		// String json = String.format(
		// "{ \"id\" : \"%d\", \"game\" : { \"name\" : \"%s\" }, \"date\" : \"%s\", \"players\" : [ %s ], \"scores\" : [ %s ], \"winner\" : \"%s\" }",
		// id,
		// game, currentDate, players, scores, winner);
		// plays.add(json);
		//
		// id++;
		// System.out.print(".");
		// if (id % 1000 == 0) {
		// System.out.println();
		// }
		// }
		// }
		// System.out.println();
		// System.out.println(game + " OK");
		// }
		// System.out.println();
		// System.out.println(currentDate + " OK");

		// appendIn("data.json", plays);
		// plays.clear();
		// }
	}

	private static <T> T getRandom(Random random, Collection<T> list) {
		int index = random.nextInt(list.size());
		return Lists.newArrayList(list).get(index);
	}

	private static int getPlayerId(Random random, int max) {
		return random.nextInt(max);
	}

	private static LocalDate getDate(LocalDate from, Random random) {
		return from.plusDays(random.nextInt(330));
	}

	private static String formatName(String accountName, Game game) {
		return accountName + "-" + game.name.toLowerCase().replaceAll("\\s", "");
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

	private static Map<String, Game> readGamesIn(String name)
			throws IOException {
		List<String> gameLines = readIn(name);
		Map<String, Game> games = new HashMap<String, Game>(gameLines.size());

		for (String line : gameLines) {
			List<String> strings = Lists.newArrayList(Splitter.on('|').split(line));
			Game game = new Game(strings.get(0), Ints.tryParse(strings.get(1)), Ints.tryParse(strings.get(2)), strings.get(3));
			games.put(strings.get(0), game);
		}

		return games;
	}

	private static <T> T pickIn(List<T> objects) {
		return objects.get(new Random().nextInt(objects.size()));
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
			if(minPlayers != null) {
				builder.append(", \"minPlayers\" : ").append(minPlayers);
			}
			if(maxPlayers != null) {
				builder.append(", \"maxPlayers\" : ").append(maxPlayers);
			}
			if(description != null && !description.isEmpty()) {
				builder.append(", \"description\" : \"").append(description).append("\"");
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

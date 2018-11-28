package me.bournedev.challenges;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import me.bournedev.challenges.utils.Util;

public class Challenge {
	private String challengeName;
	private ChallengeType challengeType;
	private List<String> objectiveObjectTypes;
	private LinkedHashMap<String, Integer> counters;
	public static ArrayList<Challenge> challenges = new ArrayList<Challenge>();

	public Challenge(String challengeName, ChallengeType challengeType, List<String> objectiveObjectTypes, LinkedHashMap<String, Integer> counters) {
		this.challengeName = challengeName;
		this.challengeType = challengeType;
		this.objectiveObjectTypes = objectiveObjectTypes;
		this.counters = counters;
	}

	public String getChallengeName() {
		return challengeName;
	}

	public ChallengeType getChallengeType() {
		return challengeType;
	}

	public List<String> getObjectiveObjectTypes() {
		return objectiveObjectTypes;
	}

	public LinkedHashMap<String, Integer> getCounters() {
		return counters;
	}

	public void setChallengeName(String challengeName) {
		this.challengeName = challengeName;
	}

	public void setChallengeType(ChallengeType challengeType) {
		this.challengeType = challengeType;
	}

	public void setObjectiveObjectType(ArrayList<String> objectiveObjects) {
		this.objectiveObjectTypes = objectiveObjects;
	}

	public void setObjectiveObjectTypes(ArrayList<String> objectiveObjectTypes) {
		this.objectiveObjectTypes = objectiveObjectTypes;
	}

	public void setCounters(LinkedHashMap<String, Integer> counters) {
		this.counters = counters;
	}

	public Integer getRanking(String playerName) {
		int ranking = 1;
		for (String key : this.counters.keySet()) {
			if (key.equals(playerName)) {
				return ranking;
			}
			ranking++;
		}
		return -1;
	}

	public void updateCounter(String key, int amount) {
		if (this.counters.containsKey(key)) {
			this.counters.put(key, this.counters.get(key) + amount);
		} else {
			this.counters.put(key, 0);
		}
	}

	public static Challenge getChallengeByName(String name) {
		for (Challenge challenge : Challenge.challenges) {
			if (challenge.getChallengeName().equals(name)) {
				return challenge;
			}
		}
		return null;
	}

	public static ArrayList<Challenge> getRandomChallenges(int amountOfRandomChallenges) {
		ArrayList<Challenge> challenges = new ArrayList<Challenge>();
		if (Challenge.challenges.size() < 5) {
			amountOfRandomChallenges = Challenge.challenges.size();
		}
		for (int i = 0; i < amountOfRandomChallenges; i++) {
			int randInt = Util.randInt(0, Challenge.challenges.size() - 1);
			Challenge challenge = Challenge.challenges.get(randInt);
			if (challenges.contains(challenge)) {
				i = i - 1;
				continue;
			}
			challenges.add(new Challenge(challenge.challengeName, challenge.challengeType, challenge.objectiveObjectTypes, challenge.counters));
		}
		return challenges;
	}
}

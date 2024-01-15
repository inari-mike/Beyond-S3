package s3.ai.rule;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import s3.base.S3;
import s3.base.S3Action;
import s3.entities.*;

public class PerceptionEngine {
	private final S3 game;
	private final String currentOwner;
	private final KnowledgeBase knowledgeBase;
	private final List<Term> cachedFacts;
	private WPlayer currentPlayer;

	public PerceptionEngine(KnowledgeBase knowledgeBase, S3 game, String playerID) {
		this.knowledgeBase = knowledgeBase;
		this.game = game;
		currentOwner = playerID;

		for (WPlayer player : game.getPlayers()) {
			if (Objects.equals(player.getOwner(), playerID)) {
				currentPlayer = player;
			}
		}

		cachedFacts = new ArrayList<>();
		updateResourceNeededToCache();
	}

	private void updateResourceNeededToCache() {
		WUnit[] units = new WUnit[] { new WTownhall(), new WPeasant(), new WBarracks(), new WGoldMine(),
				new WKnight() }; // keep consistent with action generator, WFootman/WKnight
		for (WUnit u : units) {
			cachedFacts.add(new Term("goldNeededFor", getUnitType(u), String.valueOf(u.getCost_gold())));
			cachedFacts.add(new Term("woodNeededFor", getUnitType(u), String.valueOf(u.getCost_wood())));
		}
	}

	@SuppressWarnings("SpellCheckingInspection")
	private String getUnitType(WUnit unit) {
		return switch (unit.spriteName) {
		case "townhall" -> "Base";
		case "peasant" -> "Worker";
		case "barracks" -> "Barracks";
		case "knight", "footman" -> "Light";
		case "goldmine" -> "GoldMine";
		default -> "Unknown";
		};
	}

	public void updateKnowledge() {
		knowledgeBase.clear();
		updateResourceAvailability();
		UpdateInfoForAllUnits();
		loadFactsFromCached();
	}

	private void updateResourceAvailability() {
		knowledgeBase.addTerm(new Term("goldAvailable", String.valueOf(currentPlayer.getGold())));
		knowledgeBase.addTerm(new Term("woodAvailable", String.valueOf(currentPlayer.getWood())));
	}

	private void UpdateInfoForAllUnits() {
		List<WOTree> trees = findAllTrees();
		int workersAvailable = 0;

		for (WUnit unit : game.getUnits()) {

			String unitType = getUnitType(unit);

			if (Objects.equals(unitType, "Worker") && Objects.equals(unit.getOwner(), currentOwner)) {
				workersAvailable++;
			}

			if (!Objects.equals(unitType, "Unknown")) {
				String unitId = String.valueOf(unit.getEntityID());
				knowledgeBase.addTerm(new Term("type", unitId, unitType));
				checkOneUnit(unit, unitId, unitType, trees);

			}
		}
		knowledgeBase.addTerm(new Term("workersAvailable", String.valueOf(workersAvailable)));
	}

	private List<WOTree> findAllTrees() {
		List<WOTree> trees = new ArrayList<>();
		for (int i = 0; i < game.getMap().getWidth(); i++) {
			for (int j = 0; j < game.getMap().getHeight(); j++) {
				S3PhysicalEntity e = game.getMap().getEntity(i, j);
				if (e instanceof WOTree) {
					trees.add((WOTree) e);
				}
			}
		}
		return trees;
	}

	private void checkOneUnit(WUnit unit, String unitId, String unitType, List<WOTree> trees) {

		if (Objects.equals(currentOwner, unit.getOwner())) {
			knowledgeBase.addTerm(new Term("own", unitId));

			// we only case about whether our units idle or not, not enemies
			if (unit.getStatus() == null) { // need adding logic to clear status in WTroop.cleanup()
				knowledgeBase.addTerm(new Term("idle", unitId));

				if (Objects.equals(unitType, "Worker")) {
					checkNearestTree(unit, trees); // we only case about the nearest trees for those idle workers
				}

			} else {
				// some building is going to be built, add own-it as cached fact,
				// so that it won't be over constructed
				if (unit.getStatus().m_action == S3Action.ACTION_BUILD) {
					String buildingType = (String) unit.getStatus().m_parameters.get(0);
					@SuppressWarnings("SpellCheckingInspection")
					String ownBuildingFact = Objects.equals(buildingType, "WTownhall") ? "ownBase" : "ownBarrack";
					cachedFacts.add(new Term(ownBuildingFact, "-99")); // -1 will never be used by real unit
				}
			}

		} else {
			knowledgeBase.addTerm(new Term("enemy", unitId));
		}
	}

	private void checkNearestTree(WUnit unit, List<WOTree> trees) {
		WOTree nearestTree = null;
		int leastDist = 9999;
		int unitX = unit.getX();
		int unitY = unit.getY();
		for (WOTree t : trees) {
			int dist = Math.abs(t.getX() - unitX) + Math.abs(t.getY() - unitY);
			if (dist < leastDist) {
				leastDist = dist;
				nearestTree = t;
			}
		}

		if (nearestTree != null) {
			String location = nearestTree.getX() + "," + nearestTree.getY();
			knowledgeBase.addTerm(new Term("type", location, "Tree"));
		}
	}

	public void loadFactsFromCached() {
		for (Term fact : cachedFacts) {
			knowledgeBase.addTerm(fact);
		}
	}

}

package s3.ai.rule;

import java.util.ArrayList;
import java.util.List;

import s3.base.S3;
import s3.base.S3Action;
import s3.entities.WBarracks;
import s3.entities.WKnight;
import s3.entities.WPeasant;
import s3.entities.WTownhall;
import s3.util.Pair;

public record ActionGenerator(S3 game) {

	public List<S3Action> generate(List<Term> terms) {
		List<S3Action> actions = new ArrayList<>();
		for (Term term : terms) {
			S3Action action = buildAction(term);
			if (action != null) {
				actions.add(action);
			}
		}
		return actions;
	}

	private S3Action buildAction(Term term) {
		S3Action action = null;
		int firstArg = Integer.parseInt(term.getArg(0).getValue());

		switch (term.functor) {
		case "doTrainWorker" -> action = train(firstArg, 0);
		case "doTrainKnight" -> action = train(firstArg, 1);
		case "doBuildBase" -> action = build(firstArg, 0);
		case "doBuildBarracks" -> action = build(firstArg, 1);

		default -> {
			if (term.argSize() > 1) {
				if (term.functor.equals("doAttack")) {
					int enemyId = Integer.parseInt(term.getArg(1).getValue());
					action = attack(firstArg, enemyId);

				} else if (term.functor.equals("doHarvest")) {
					action = harvest(term);
				}
			}
		}
		}
		return action;
	}

	private S3Action attack(int attackerId, int enemyId) {
		return new S3Action(attackerId, S3Action.ACTION_ATTACK, enemyId);
	}

	private S3Action harvest(Term t) {
		int workerId = Integer.parseInt(t.getArg(0).getValue());

		if (t.getArg(1).getValue().contains(",")) { // chop wood
			String[] treeLocation = t.getArg(1).getValue().split(",");
			int treeX = Integer.parseInt(treeLocation[0]);
			int treeY = Integer.parseInt(treeLocation[1]);
			return new S3Action(workerId, S3Action.ACTION_HARVEST, treeX, treeY);

		} else { // mine gold
			int goldMineId = Integer.parseInt(t.getArg(1).getValue());
			return new S3Action(workerId, S3Action.ACTION_HARVEST, goldMineId);
		}
	}

	private S3Action train(int trainerId, int characterType) {
		if (characterType != 0 && characterType != 1) {
			return null;
		}
		return new S3Action(trainerId, S3Action.ACTION_TRAIN,
				characterType == 0 ? WPeasant.class.getSimpleName() : WKnight.class.getSimpleName());
	}

	private S3Action build(int workerId, int buildingType) {
		if (buildingType != 0 && buildingType != 1) {
			return null;
		}
		WPeasant peasant = (WPeasant) game.getUnit(workerId);
		int peasantX = peasant.getX();
		int peasantY = peasant.getY();
		int buildingWidth = buildingType == 0 ? new WTownhall().getWidth() : new WBarracks().getWidth();
		Pair<Integer, Integer> location = game.findFreeSpace(peasantX, peasantY, buildingWidth + 2);

		if (location == null) {
			location = game.findFreeSpace(peasantX, peasantY, buildingWidth);
			if (location == null) {
				return null;
			}
		} else {
			location.m_a++;
			location.m_b++;
		}

		String buildingName = buildingType == 0 ? WTownhall.class.getSimpleName() : WBarracks.class.getSimpleName();
		return new S3Action(workerId, S3Action.ACTION_BUILD, buildingName, location.m_a, location.m_b);
	}
}

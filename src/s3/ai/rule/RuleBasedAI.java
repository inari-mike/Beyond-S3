package s3.ai.rule;

import java.util.List;

import s3.base.S3;
import s3.base.S3Action;
import s3.entities.WPlayer;

public class RuleBasedAI implements s3.ai.AI {

	private final PerceptionEngine perceptionEngine;
	private final InferenceEngine inferenceEngine;
	private final ActionGenerator actionGenerator;
	public String m_playerID;

	public RuleBasedAI(String playerID, S3 game) {
		m_playerID = playerID;
		KnowledgeBase knowledgeBase = new KnowledgeBase();
		perceptionEngine = new PerceptionEngine(knowledgeBase, game, playerID);
		List<Rule> rules = new RuleLoader("src/s3/ai/rule/rules-S3.txt").getRules();
		inferenceEngine = new InferenceEngine(knowledgeBase, rules);
		actionGenerator = new ActionGenerator(game);
	}

	@Override
	public void game_cycle(S3 game, WPlayer player, List<S3Action> actions) {
		perceptionEngine.updateKnowledge();
		List<Term> triggeredEffects = inferenceEngine.inference();
		List<S3Action> firedActions = actionGenerator.generate(triggeredEffects);
		actions.addAll(firedActions);
	}

	@Override
	public String getPlayerId() {
		return m_playerID;
	}

	@Override
	public void gameStarts() {
	}

	@Override
	public void gameEnd() {

	}

}

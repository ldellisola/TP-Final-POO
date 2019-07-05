package game.frontend;

import game.backend.CandyGame;
import game.backend.GameListener;
import game.backend.cell.Cell;
import game.backend.cell.GoldenCell;
import game.backend.element.Element;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.geometry.Point2D;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

public class CandyFrame extends VBox {

	private static final int CELL_SIZE = 65;

	private BoardPanel boardPanel;
	private ScorePanel scorePanel;
	private ImageManager images;
	private Point2D lastPoint;
	private CandyGame game;
	private MovementsPanel movements;
//	private GoalPanel goals;

	public CandyFrame(CandyGame game) {
		this.game = game;
		getChildren().add(new AppMenu());
		images = new ImageManager();
		boardPanel = new BoardPanel(game.getSize(), game.getSize(), CELL_SIZE);
		getChildren().add(boardPanel);
		scorePanel = new ScorePanel();
		getChildren().add(scorePanel);
		movements = new MovementsPanel();
		getChildren().add(movements);
		//goals = new GoalPanel();
		//getChildren().add(goals);

		game.initGame();

		setUpGameListener();

		addEventHandler(MouseEvent.MOUSE_CLICKED, event -> {
			if (lastPoint == null) {
				lastPoint = translateCoords(event.getX(), event.getY());

				System.out.println("Get first = " +  lastPoint);
			} else {
				Point2D newPoint = translateCoords(event.getX(), event.getY());
				if (newPoint != null) {
					System.out.println("Get second = " +  newPoint);
					game().tryMove((int)lastPoint.getX(), (int)lastPoint.getY(), (int)newPoint.getX(), (int)newPoint.getY());
					String message = ((Long)game().getScore()).toString();
					movements.update(game().currMovements(),game().maxMovements());
					//goals.update(game().getCurrentGoal(),game().getGoal(),game().getGoalDescription());
					if (game().isFinished()) {
						if (game().playerWon()) {
							message = message + " Finished - Player Won!";
						} else {
							message = message + " Finished - Loser !";
						}
						scorePanel.updateScore(message);

						if(game().playerWon())
							new WonLevelAlert(game());
						else
							new LostLevelAlert(game());

						setUpGameListener();
					}
					else {
						scorePanel.updateScore(message);
					}

					lastPoint = null;
				}
			}
		});

	}

	protected void setUpGameListener(){
		GameListener listener;
		game.addGameListener(new BasicGameListener());

		game.addGameListener(new GoldenGameListener(game(),boardPanel));

		GoalListener list = new GoalListener(game());
		getChildren().add(list.getPanel());
		game.addGameListener(list);

		movements.update(game().currMovements(),game().maxMovements());

		game().updateListeners();
	}

	private CandyGame game() {
		return game;
	}

	private Point2D translateCoords(double x, double y) {
		double i = x / CELL_SIZE;
		double j = y / CELL_SIZE;
		return (i >= 0 && i < game.getSize() && j >= 0 && j < game.getSize()) ? new Point2D(j, i) : null;
	}

	private class BasicGameListener implements GameListener{
		@Override
		public void gridUpdated() {
			Timeline timeLine = new Timeline();
			Duration frameGap = Duration.millis(100);
			Duration frameTime = Duration.ZERO;
			for (int i = game().getSize() - 1; i >= 0; i--) {
				for (int j = game().getSize() - 1; j >= 0; j--) {
					int finalI = i;
					int finalJ = j;
					Cell cell = CandyFrame.this.game.get(i, j);
					Element element = cell.getContent();
					Image image = images.getImage(element);

					timeLine.getKeyFrames().add(new KeyFrame(frameTime, e -> boardPanel.setImage(finalI, finalJ, null)));
					timeLine.getKeyFrames().add(new KeyFrame(frameTime, e -> boardPanel.setImage(finalI, finalJ, image)));
				}
				frameTime = frameTime.add(frameGap);
			}
			timeLine.play();
		}
		@Override
		public void cellExplosion(Element e) {
			//
		}
	}




}

package oogasalad.player.model.strategies.control;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import oogasalad.engine.config.EntityPlacement;
import oogasalad.engine.records.config.model.controlConfig.TargetControlConfigRecord;
import oogasalad.engine.records.model.EntityTypeRecord;
import oogasalad.engine.utility.constants.Directions.Direction;
import oogasalad.player.model.Entity;
import oogasalad.player.model.GameMapInterface;
import oogasalad.player.model.api.PathFindingStrategyFactory;
import oogasalad.player.model.api.TargetStrategyFactory;
import oogasalad.player.model.strategies.control.pathfinding.PathFindingStrategyInterface;
import oogasalad.player.model.strategies.control.targetcalculation.TargetStrategyInterface;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

class TargetControlStrategyTest {

  private GameMapInterface gameMap;
  private EntityPlacement placement;
  private Entity entity;

  @BeforeEach
  void setup() {
    gameMap = mock(GameMapInterface.class);
    placement = mock(EntityPlacement.class);
    EntityTypeRecord entityType = mock(EntityTypeRecord.class);
    entity = mock(Entity.class);

    when(placement.getX()).thenReturn(5.0);
    when(placement.getY()).thenReturn(5.0);
    when(placement.getType()).thenReturn(entityType);
    when(entity.getEntityDirection()).thenReturn(Direction.R);
    when(gameMap.iterator()).thenReturn(Collections.emptyIterator());
  }

  @Test
  void update_targetToRight_setsDirectionRight() {
    runUpdateTest(new int[]{6, 5}, new int[]{1, 0}, Direction.R, true);
  }

  @Test
  void update_targetToLeft_setsDirectionLeft() {
    runUpdateTest(new int[]{4, 5}, new int[]{-1, 0}, Direction.L, true);
  }

  @Test
  void update_targetAbove_setsDirectionUp() {
    runUpdateTest(new int[]{5, 4}, new int[]{0, -1}, Direction.U, true);
  }

  @Test
  void update_targetBelow_setsDirectionDown() {
    runUpdateTest(new int[]{5, 6}, new int[]{0, 1}, Direction.D, true);
  }

  @Test
  void update_cannotMoveInDirection_doesNotSetDirection() {
    runUpdateTest(new int[]{6, 5}, new int[]{1, 0}, Direction.R, false);
  }

  private void runUpdateTest(int[] targetPosition, int[] pathVector,
      Direction expectedDirection, boolean canMove) {
    TargetStrategyInterface mockTargetStrategy = mock(TargetStrategyInterface.class);
    PathFindingStrategyInterface mockPathStrategy = mock(PathFindingStrategyInterface.class);

    when(mockTargetStrategy.getTargetPosition()).thenReturn(targetPosition);
    when(mockPathStrategy.getPath(eq(gameMap), eq(5), eq(5),
        eq(targetPosition[0]), eq(targetPosition[1]), eq(placement), eq(Direction.R)))
        .thenReturn(pathVector);
    when(entity.canMove(expectedDirection)).thenReturn(canMove);

    try (
        MockedStatic<TargetStrategyFactory> mockTargetFactory = mockStatic(
            TargetStrategyFactory.class);
        MockedStatic<PathFindingStrategyFactory> mockPathFactory = mockStatic(
            PathFindingStrategyFactory.class)
    ) {
      mockTargetFactory.when(() -> TargetStrategyFactory.createTargetStrategy(placement, gameMap))
          .thenReturn(mockTargetStrategy);
      mockPathFactory.when(() -> PathFindingStrategyFactory.createPathFindingStrategy("Dijkstra"))
          .thenReturn(mockPathStrategy);

      TargetControlStrategy strategy = new TargetControlStrategy(
          gameMap,
          placement,
          new TargetControlConfigRecord("Dijkstra", null, null)
      );

      strategy.update(entity);

      if (canMove) {
        verify(entity).setEntitySnapDirection(expectedDirection);
      } else {
        verify(entity, never()).setEntitySnapDirection(any(Direction.class));
      }
    }
  }
}

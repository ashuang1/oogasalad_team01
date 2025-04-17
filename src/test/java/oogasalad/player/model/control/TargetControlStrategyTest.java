package oogasalad.player.model.control;

import oogasalad.engine.enums.Directions.Direction;
import oogasalad.engine.model.GameMap;
import oogasalad.engine.model.controlConfig.TargetControlConfig;
import oogasalad.engine.model.EntityPlacement;
import oogasalad.engine.model.EntityType;
import oogasalad.engine.model.entity.Entity;
import oogasalad.player.model.control.pathfinding.PathFindingStrategyInterface;
import oogasalad.player.model.control.pathfinding.PathFindingStrategyFactory;
import oogasalad.player.model.control.targetcalculation.TargetStrategyInterface;
import oogasalad.player.model.control.targetcalculation.TargetStrategyFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.util.Collections;

import static org.mockito.Mockito.*;

class TargetControlStrategyTest {

  private GameMap gameMap;
  private EntityPlacement placement;
  private EntityType entityType;
  private Entity entity;

  @BeforeEach
  void setup() {
    gameMap = mock(GameMap.class);
    placement = mock(EntityPlacement.class);
    entityType = mock(EntityType.class);
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
        MockedStatic<TargetStrategyFactory> mockTargetFactory = mockStatic(TargetStrategyFactory.class);
        MockedStatic<PathFindingStrategyFactory> mockPathFactory = mockStatic(PathFindingStrategyFactory.class)
    ) {
      mockTargetFactory.when(() -> TargetStrategyFactory.createTargetStrategy(placement, gameMap))
          .thenReturn(mockTargetStrategy);
      mockPathFactory.when(() -> PathFindingStrategyFactory.createPathFindingStrategy("Dijkstra"))
          .thenReturn(mockPathStrategy);

      TargetControlStrategy strategy = new TargetControlStrategy(
          gameMap,
          placement,
          new TargetControlConfig("Dijkstra", null)
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

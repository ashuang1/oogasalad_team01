package oogasalad.player.view;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import javafx.scene.canvas.GraphicsContext;
import oogasalad.engine.config.EntityPlacement;
import oogasalad.engine.utility.constants.Directions.Direction;
import oogasalad.player.model.Entity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import util.DukeApplicationTest;

class EntityViewTest extends DukeApplicationTest {

  private Entity mockEntity;
  private GraphicsContext mockGC;

  @BeforeEach
  void setup() {
    mockEntity = mock(Entity.class);
    EntityPlacement mockPlacement = mock(EntityPlacement.class);
    mockGC = mock(GraphicsContext.class);

    when(mockEntity.getEntityPlacement()).thenReturn(mockPlacement);
    when(mockPlacement.getEntityImagePath()).thenReturn("assets/pacman.png");
    when(mockPlacement.getEntityFrameNumber()).thenReturn(6);
    when(mockPlacement.getTypeString()).thenReturn("assets/pacman.png");
    when(mockPlacement.getCurrentFrame()).thenReturn(1);
    when(mockPlacement.getX()).thenReturn(3.0);
    when(mockPlacement.getY()).thenReturn(4.0);
    when(mockPlacement.getEntityImageHeight()).thenReturn(28);
    when(mockPlacement.getEntityImageWidth()).thenReturn(28);

  }

  // The below tests use the pacman sprite as an example.
  // They ensure that the correct sprite is provided for each of the possible movement directions.
  // If additional movement actions are permitted then this should be updated below
  // Code refactored using ChatGPT.
  @Test
  void draw_RightDirectionSpritePacMan_CorrectDrawCall() {
    testDrawDirection(Direction.R);
  }

  @Test
  void draw_LeftDirectionSpritePacMan_CorrectDrawCall() {
    testDrawDirection(Direction.L);
  }

  @Test
  void draw_UpDirectionSpritePacMan_CorrectDrawCall() {
    testDrawDirection(Direction.U);
  }

  @Test
  void draw_DownDirectionSpritePacMan_CorrectDrawCall() {
    testDrawDirection(Direction.D);
  }

  private void testDrawDirection(Direction direction) {
    when(mockEntity.getEntityDirection()).thenReturn(direction);

    double entity_height = mockEntity.getEntityPlacement().getEntityImageHeight();
    double entity_width = mockEntity.getEntityPlacement().getEntityImageWidth();
    double offsetX = mockEntity.getEntityPlacement().getCurrentFrame() * entity_width;
    double dirOffset = 0;

    if (direction == Direction.L) {
      dirOffset = (int) entity_height;
    }
    if (direction == Direction.U) {
      dirOffset = (int) (2 * entity_height);
    }
    if (direction == Direction.D) {
      dirOffset = (int) (3 * entity_height);
    }

    EntityView view = new EntityView(mockEntity, "data/games/BasicPacMan/");
    view.draw(mockGC, 28, 28);

    verify(mockGC).drawImage(
        any(),                             // image
        eq(offsetX),                 // sourceX
        eq(dirOffset),                       // sourceY
        eq(entity_width),                           // sourceWidth
        eq(entity_height),                           // sourceHeight
        eq(3.0 * 28),                      // destX
        eq(4.0 * 28),                      // destY
        eq(28.0),                          // destWidth
        eq(28.0)                           // destHeight
    );
  }
}

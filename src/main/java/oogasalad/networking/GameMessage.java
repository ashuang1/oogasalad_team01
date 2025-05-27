package oogasalad.networking;

import oogasalad.engine.utility.constants.Directions.Direction;

public record GameMessage(MessageType type, int playerId, Direction direction) {}

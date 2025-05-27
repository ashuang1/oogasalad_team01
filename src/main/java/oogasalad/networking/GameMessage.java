package oogasalad.networking;

import java.util.Map;

public record GameMessage(MessageType type, int playerId, Map<String, Object> payload) {}

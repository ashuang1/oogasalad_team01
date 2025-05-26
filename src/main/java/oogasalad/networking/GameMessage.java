package oogasalad.networking;

public record GameMessage(MessageType type, int playerId, String direction) {}

/*
GameState.java enum class stores the basic game states that will be used by the Game Manager
to track the current game state.
*/
package Game.Logic;

public enum GameState {
    MAIN_MENU, // Entry point - start game, settings, terminate
    RUNNING,   // Game loop - step loop, pause, game over
    PAUSED,    // Pause loop - resume loop, settings, terminate
    GAME_OVER, // End loop - main menu, new game, terminate
    COUNTDOWN // Ensure synchronization
}
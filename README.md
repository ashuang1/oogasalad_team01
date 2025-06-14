## PacOne - Game Authoring and Player Environment

### Credit

This project was completed as a group project for the class CS308, Advanced Software Design and
Implementation.

### Authors

Owen Jennings, Jessica Chen, Angela Predolac, William He, Luke Fu, Austin Huang, Troy Ludwig, Ishan
Madan

### Description

This project implements an authoring environment and player for the traditional arcade game,
Pac-Man and supports multiple game variations.

### Timeline

* Start Date: March 21st, 2025

* Finish Date: May 2nd, 2025

* Hours Spent: 350 hr (approximately)

### Attributions

* Resources used for learning (including AI assistance)
    * Java and JavaFX online documentation.

* Resources used directly (including AI assistance)
    * Our project uses Google Fonts for our game fonts. The specific fonts we use are Inter and
      Press Start 2P.
    * Various sprites from the original game were used. Additionally, this source was used for game
      sprites: https://www.spriters-resource.com/arcade/pacman/
    * For the Cat and Mouse game got insporation from this
      game: https://www.cbc.ca/kids/games/play/pac-rat, aesprite to recreate some of these sprites
    * Additional game sprites for other games were modified from https://www.spriters-resource.com/
        * This specific sprite sheet was modified for the Flap-Man
          game: https://www.spriters-resource.com/mobile/flappybird/sheet/59894/
    * Image editing software for
      sprites: https://imageonline.co/repeat-image-generator.php, https://merge.imageonline.co/
    * For the dinasour
      games: https://www.spriters-resource.com/browser_games/googledinosaurrungame/sheet/160414/
    * For some of the editing and creation of sprite sheets used Aesprite
    * Some styles were used from the previous cell society project.
    * Some of the javadocs were written by AI
    * Some members of the team used ChatGPT to assist with debugging and generating code. When
      applicable, code generated by ChatGPT or other large language models (LLMs) was cited to the
      best of our ability.
        * Citation styles varied among team members. Most citations were provided as in-line
          comments at the beginning of classes or methods.

### Running the Program

* Main class:
    * src/main/java/oogasalad/Main.java

* Data files needed:
    * The files within /data for loading game player games as well as the wizard system and within
      /src/main/resources for internal documentation, language support, and css styling.

* Interesting data files:
    * The files in data/games/BasicPacMan code pacman mostly as in the arcade game with a few
      modifications
    * The files in data/games/CatAndMouse show how you can take the regular pacman game and reskin
      it to give it a new look
    * Data files within data/games/Flap-Man and data/games/PacSwitch show how you can use the same
      mechanics and structure to implement the pacman game to make very different feeling games such
      as puzzle games (PacSwitch) and runner/platformer games (Flap-Man)

* Key/Mouse inputs:
    * Cheat Keys
        * N - next level
        * R - reset
        * = - add life
        * S - speed up
        * P - puase
        * These are only available if they are configured in the associated game's config
    * Movement
        - Player movement is done with the arrow keys, for most of the games all arrow keys will
          move the player up, down, left, right; for certain games either through the control
          strategy or using blockers, some of these options will be not available, but movement is
          done through arrow keys.
    * Help in Authoring Environment
        - F1 - opens help menu in authoring environment

### Notes/Assumptions

* Assumptions or Simplifications:
    * Simplifications from the normal pacman game
        * Pacman for target ahead based on the way they encode math and coordinate vectors, has a "
          bit error" where you target above while moving up the entity targets the position up and
          right, this was not implemented
        * Pacman's scatter timings do not work exactly like the timings of the original games
    * For the challenging extension "Play Modes" we assumed "Allow users to play a challenge mode,
      through any number of randomly selected levels" that keeping the same number of levels to win
      the game but randomizing the levels you play to fufill this number of levels to count.

* Known Bugs:
    * If you restart a game, if an entity was wrapping on restart, it throws an invalid placement
      error when you try to reset, however if you return to home and click the game again, the error
      does not persist.
    * The player's movement is maintained after restarting (i.e if you were moving right and died,
      the next game you play, your player continues moving right).
    * If you restart a game, you can no longer pause the game, the button does not function and you
      have to return to the home screen to fix this behavior.
      Since these bugs are not game breaking, we just did not have time to fix them before the code
      freeze.

* Features implemented:
    * Authoring Environment
        * Choose and place game elements (create an entity type, drag to the center grid)
        * Set up graphical elements
            * For each entity type and mode can configure its sprites
            * For each level can add a background image
        * Can change game settings and collision rules, additionally can create spawn events/mode
          changes triggered through time/score information
        * Can change in entity type and their modes what way the user can control the entity or what
          AI it uses to move
        * Can configure the levels and add levels in the authoring environment
        * Can update game metadata where you can include instructions and creator information
        * Can upload game configs to continue editing in the authoring environment

    * Preferences such as theme that can be set on the splash screen persists between program runs

    * Game Player
        * Show any number of games automatically, can add a new game by putting the game folder (
          i.e. from the output of authoring environment) into data/games
        * On the splash screen can see information about each game by clicking the i icon under the
          card for the game
        * Using return to home can select any game to play without restarting the program
        * Pause a game during play by using the pause button at the top or the pause cheat code when
          enabled.
        * Can see HUD information such as high scores (which are through successive runs of the
          program), score, lives, etc.
        * Save progress in a game to restart later, the specific save points you can save progress
          are just between levels. You can do this manually by hitting save game between the level
          transitions, it also does it automatically by saving your level every so often, then if
          you exit and replay, the game will load on your last level.

    * Basic Extentions
        * Artificial players: the ghost use many different targeting strategies in order to provide
          different options to make more advanced targeting (i.e. red ghost follows you, pinky
          targets ahead to try cutting off, blue ghost uses pinky's location to chose a target to
          help target the entity with pinky.)
        * Help system in authoring environment

    * Challenging Extentions
        * Play modes, allow you to play the game with the same number of levels to beat but you play
          a random level for each new each level (i.e. you can play level 1 for all levels...)

* Features unimplemented:
    * Randomized challenge levels is not stored within the save, thus the randomize levels work if
      you keep playing the game, but are not maintained if you save the game and reload it (you will
      play a normal ordered game but the number of levels you have passed is maintained.)
    * Playing multiple different games at once

* Noteworthy Features:
    * In authoring environment can configure cheat codes to add to your game, if these are added,
      then within the game, the cheat key can be used.
    * Infinitely spawning levels can be created with a combination of spawn entity events and the
      reset time collision strategy (see Flap-Man for an example of infinitely spawning pipes).


package serangooner;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

// Playback authored with the help of Codex.
/**
 * Plays the funny sound that accompanies a rejected command.
 * Playback is a side show: it happens on a separate thread so that it never
 * delays the next prompt, and is skipped when the audio file is absent or
 * when no one is listening, as in a test.
 */
public class SoundPlayer {
    private static final Path SOUND_FILE = Path.of("src/main/resources/faaah.mp3");

    private final boolean isEnabled;

    /**
     * Constructs a player that may or may not make a sound.
     *
     * @param isEnabled True to play the sound, false to stay silent.
     */
    public SoundPlayer(boolean isEnabled) {
        this.isEnabled = isEnabled;
    }

    /**
     * Plays the sound once, and does nothing at all if it cannot be played.
     */
    public void play() {
        if (!isEnabled || !Files.exists(SOUND_FILE)) {
            return;
        }

        Thread.ofVirtual().start(() -> {
            try {
                new ProcessBuilder("afplay", SOUND_FILE.toString()).start().waitFor();
            } catch (IOException | InterruptedException exception) {
                Thread.currentThread().interrupt();
            }
        });
    }
}

package serangooner.ui;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import org.junit.jupiter.api.Test;

public class SoundPlayerTest {
    // Only the disabled player is exercised: an enabled one shells out to the
    // system audio player, which a test run must not do.

    @Test
    public void play_disabled_staysSilentWithoutFailing() {
        assertDoesNotThrow(() -> new SoundPlayer(false).play());
    }

    @Test
    public void play_disabledAndCalledRepeatedly_staysSilentWithoutFailing() {
        SoundPlayer player = new SoundPlayer(false);
        assertDoesNotThrow(() -> {
            player.play();
            player.play();
            player.play();
        });
    }
}

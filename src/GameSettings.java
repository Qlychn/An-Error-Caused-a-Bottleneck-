public final class GameSettings {

    // displayMode values
    public static final byte DISPLAY_FULLSCREEN = 0;
    public static final byte DISPLAY_BORDERLESS = 1;

    public static byte monitorIndex = 0;             
    public static byte displayMode = DISPLAY_FULLSCREEN;

    public static byte masterVolume = 100;               // 0-100
    public static byte musicVolume = 80;                 // 0-100
    public static byte sfxVolume = 80;                   // 0-100

    private GameSettings() {}

    /** Final music volume as 0..1 (master x music). Pass this to Music.setVolume(). */
    public static float effectiveMusicVolume() {
        return (masterVolume / 100f) * (musicVolume / 100f);
    }

    /** Final sfx volume as 0..1 (master x sfx). Pass this to Sound.play(volume). */
    public static float effectiveSfxVolume() {
        return (masterVolume / 100f) * (sfxVolume / 100f);
    }
}
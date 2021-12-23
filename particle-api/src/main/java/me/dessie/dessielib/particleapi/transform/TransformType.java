package me.dessie.dessielib.particleapi.transform;

public enum TransformType {
    /**
     * The step count for the transformation will restart at 0 once the frame cap is reached.
     */
    RESTART,

    /**
     * The step count will start working backwards back to 0 once the frame cap is reached.
     */
    OSCILLATE,

    /**
     * The transformation does not have a frame count, and is always applied as-is.
     */
    STATIC
}

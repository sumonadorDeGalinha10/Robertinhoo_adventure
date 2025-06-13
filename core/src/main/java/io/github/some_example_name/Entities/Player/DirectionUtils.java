package io.github.some_example_name.Entities.Player;
import io.github.some_example_name.Entities.Renderer.WeaponAnimations;

public class DirectionUtils {
    public static WeaponAnimations.WeaponDirection getDirectionFromAngle(float angle) {
        angle = (angle + 360) % 360;

        if (angle >= 337.5 || angle < 22.5) {
            return WeaponAnimations.WeaponDirection.E;
        } else if (angle >= 22.5 && angle < 67.5) {
            return WeaponAnimations.WeaponDirection.NE;
        } else if (angle >= 67.5 && angle < 112.5) {
            return WeaponAnimations.WeaponDirection.N;
        } else if (angle >= 112.5 && angle < 157.5) {
            return WeaponAnimations.WeaponDirection.NW;
        } else if (angle >= 157.5 && angle < 202.5) {
            return WeaponAnimations.WeaponDirection.W;
        } else if (angle >= 202.5 && angle < 247.5) {
            return WeaponAnimations.WeaponDirection.SW;
        } else if (angle >= 247.5 && angle < 292.5) {
            return WeaponAnimations.WeaponDirection.S;
        } else if (angle >= 292.5 && angle < 337.5) {
            return WeaponAnimations.WeaponDirection.SE;
        }
        return null;
    }
}

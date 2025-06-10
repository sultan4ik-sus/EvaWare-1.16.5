package eva.ware.utils.render.other;

public class GifUtility {
    public int getFrame(int totalFrames, int frameDelay, boolean countFromZero) {
        long currentTime = System.currentTimeMillis();
        int frameIndex = (int) (currentTime / frameDelay % totalFrames);
        return countFromZero ? frameIndex : frameIndex + 1;
    }
}

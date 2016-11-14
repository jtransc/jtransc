package java.awt.image;

public interface ImageProducer {
	public void addConsumer(ImageConsumer ic);
	public boolean isConsumer(ImageConsumer ic);
	public void removeConsumer(ImageConsumer ic);
	public void startProduction(ImageConsumer ic);
	public void requestTopDownLeftRightResend(ImageConsumer ic);
}

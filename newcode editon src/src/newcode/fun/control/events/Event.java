package newcode.fun.control.events;

public class Event {

    public void cancel() {
        isCancel = true;
    }
    public void open() {
        isCancel = false;
    }

    public boolean isCancel;

    public boolean isCancel() {
        return isCancel;
    }

    public void setCancel(boolean cancel) {
        this.isCancel = cancel;
    }

}
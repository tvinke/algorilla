import java.util.List;

public class NotificationService {
    public void broadcastAll(List<Channel> channels) {
        channels.stream().forEach(ch -> deliver(ch));
    }

    private void deliver(Channel channel) {
        for (Subscriber sub : channel.getSubscribers()) {
            sub.notify(channel.getMessage());
        }
    }
}

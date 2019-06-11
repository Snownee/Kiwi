package snownee.kiwi.network;

public class ClientPacket extends Packet
{

    @Override
    public void send()
    {
        NetworkChannel.INSTANCE.channel.sendToServer(this);
    }

}

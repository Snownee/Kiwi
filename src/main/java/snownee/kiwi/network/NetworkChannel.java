//package snownee.kiwi.network;
//
//import io.netty.buffer.ByteBuf;
//import io.netty.buffer.Unpooled;
//import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
//import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
//import it.unimi.dsi.fastutil.objects.Object2IntArrayMap;
//import it.unimi.dsi.fastutil.objects.Object2IntMap;
//import net.minecraft.client.Minecraft;
//import net.minecraft.client.entity.EntityPlayerSP;
//import net.minecraft.entity.player.EntityPlayerMP;
//import net.minecraft.network.NetHandlerPlayServer;
//import net.minecraft.network.PacketBuffer;
//import net.minecraft.util.math.BlockPos;
//import net.minecraftforge.fml.common.FMLCommonHandler;
//import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
//import net.minecraftforge.fml.common.network.FMLEventChannel;
//import net.minecraftforge.fml.common.network.FMLNetworkEvent;
//import net.minecraftforge.fml.common.network.NetworkRegistry;
//import net.minecraftforge.fml.common.network.internal.FMLProxyPacket;
//import net.minecraftforge.fml.relauncher.Side;
//import net.minecraftforge.fml.relauncher.SideOnly;
//import snownee.kiwi.Kiwi;
//
//public final class NetworkChannel
//{
//    public static final NetworkChannel INSTANCE = new NetworkChannel();
//
//    private final Object2IntMap<Class<? extends PacketMod>> mapping = new Object2IntArrayMap<>();
//    private final Int2ObjectMap<Class<? extends PacketMod>> mappingReverse = new Int2ObjectArrayMap<>();
//    private int nextIndex = 0;
//    private final FMLEventChannel channel;
//
//    private NetworkChannel()
//    {
//        (channel = NetworkRegistry.INSTANCE.newEventDrivenChannel(Kiwi.MODID)).register(this);
//    }
//
//    @SubscribeEvent
//    public void onServerPacketIncoming(FMLNetworkEvent.ServerCustomPacketEvent event)
//    {
//        handleOnServer(decodeData(event.getPacket().payload()), ((NetHandlerPlayServer) event.getHandler()).player);
//    }
//
//    @SideOnly(Side.CLIENT)
//    @SubscribeEvent
//    public void onClientPacketIncoming(FMLNetworkEvent.ClientCustomPacketEvent event)
//    {
//        handleOnClient(decodeData(event.getPacket().payload()), Minecraft.getMinecraft().player);
//    }
//
//    public void sendToAll(PacketMod packet)
//    {
//        channel.sendToAll(new FMLProxyPacket(new PacketBuffer(unpack(packet)), Kiwi.MODID));
//    }
//
//    public void sendToAllTracking(PacketMod packet, int dim, BlockPos pos)
//    {
//        channel.sendToAllTracking(new FMLProxyPacket(new PacketBuffer(unpack(packet)), Kiwi.MODID), new NetworkRegistry.TargetPoint(dim, pos.getX(), pos.getY(), pos.getZ(), 4));
//    }
//
//    public void sendToAllAround(PacketMod packet, int dim, BlockPos pos)
//    {
//        sendToAllAround(packet, dim, pos.getX(), pos.getY(), pos.getZ(), 16D);
//    }
//
//    public void sendToAllAround(PacketMod packet, int dim, double x, double y, double z, double range)
//    {
//        channel.sendToAllAround(new FMLProxyPacket(new PacketBuffer(unpack(packet)), Kiwi.MODID), new NetworkRegistry.TargetPoint(dim, x, y, z, range));
//    }
//
//    public void sendToDimension(PacketMod packet, int dim)
//    {
//        channel.sendToDimension(new FMLProxyPacket(new PacketBuffer(unpack(packet)), Kiwi.MODID), dim);
//    }
//
//    public void sendToPlayer(PacketMod packet, EntityPlayerMP player)
//    {
//        channel.sendTo(new FMLProxyPacket(new PacketBuffer(unpack(packet)), Kiwi.MODID), player);
//    }
//
//    public void sendToServer(PacketMod packet)
//    {
//        channel.sendToServer(new FMLProxyPacket(new PacketBuffer(unpack(packet)), Kiwi.MODID));
//    }
//
//    public void register(Class<? extends PacketMod> klass)
//    {
//        mapping.put(klass, nextIndex);
//        mappingReverse.put(nextIndex, klass);
//        nextIndex++;
//    }
//
//    private int getPacketIndex(Class<? extends PacketMod> klass)
//    {
//        return mapping.getInt(klass);
//    }
//
//    private PacketMod getByIndex(int index)
//    {
//        try
//        {
//            return mappingReverse.get(index).newInstance();
//        }
//        catch (Exception e)
//        {
//            return null;
//        }
//    }
//
//    private PacketMod decodeData(ByteBuf buffer)
//    {
//        final int index = buffer.readInt();
//        PacketMod packet = this.getByIndex(index);
//        if (packet == null)
//        {
//            Kiwi.logger.error("Receiving malformed packet");
//            return null;
//        }
//        packet.readDataFrom(buffer);
//        return packet;
//    }
//
//    @SideOnly(Side.CLIENT)
//    private void handleOnClient(PacketMod packet, EntityPlayerSP player)
//    {
//        Minecraft.getMinecraft().addScheduledTask(() -> {
//            try
//            {
//                packet.handleClient(player);
//            }
//            catch (Exception e)
//            {
//                Kiwi.logger.catching(e);
//            }
//        });
//    }
//
//    private void handleOnServer(PacketMod packet, EntityPlayerMP player)
//    {
//        FMLCommonHandler.instance().getMinecraftServerInstance().addScheduledTask(() -> {
//            try
//            {
//                packet.handleServer(player);
//            }
//            catch (Exception e)
//            {
//                Kiwi.logger.catching(e);
//            }
//        });
//    }
//
//    private ByteBuf unpack(PacketMod packet)
//    {
//        ByteBuf buffer = Unpooled.buffer();
//        buffer.writeInt(this.getPacketIndex(packet.getClass()));
//        try
//        {
//            packet.writeDataTo(buffer);
//        }
//        catch (Exception e)
//        {
//            Kiwi.logger.catching(e);
//        }
//        return buffer;
//    }
//}

package fi.dy.masa.malilib.network.packet;

/**
 * Example PacketType file for Downstream Mods
 */
public class PacketType
{
    public record CarpetHello()
    {
        //public static int PROTOCOL_VERSION = 1;
        public static String HI = "69";
        public static String HELLO = "420";
    }
}

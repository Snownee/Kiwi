//package snownee.kiwi.util;
//
//import com.google.common.collect.Lists;
//import net.minecraft.entity.player.EntityPlayer;
//import net.minecraft.util.EnumFacing;
//import net.minecraft.util.math.AxisAlignedBB;
//import net.minecraft.util.math.RayTraceResult;
//import net.minecraft.util.math.Vec3d;
//
//import javax.vecmath.Point2d;
//import java.util.Comparator;
//import java.util.List;
//
//public class AABBUtil
//{
//    public static AxisAlignedBB rotate(AxisAlignedBB aabb, EnumFacing facing)
//    {
//        Point2d pointMin = rotate(new Point2d(aabb.minX, aabb.minZ), facing);
//        Point2d pointMax = rotate(new Point2d(aabb.maxX, aabb.maxZ), facing);
//        return new AxisAlignedBB(pointMin.x, aabb.minY, pointMin.y, pointMax.x, aabb.maxY, pointMax.y);
//    }
//
//    public static Point2d rotate(Point2d point, EnumFacing facing)
//    {
//        double x = point.x - 0.5;
//        double y = point.y - 0.5;
//        Point2d pointNew = new Point2d();
//        pointNew.x = facing.getHorizontalIndex() % 2 == 0 ? x : y;
//        if (facing.getHorizontalIndex() < 2)
//        {
//            pointNew.x *= -1;
//        }
//        pointNew.y = facing.getHorizontalIndex() % 2 == 0 ? y : x;
//        if (facing.getHorizontalIndex() == 0 || facing.getHorizontalIndex() == 3)
//        {
//            pointNew.y *= -1;
//        }
//        pointNew.x += 0.5;
//        pointNew.y += 0.5;
//        return pointNew;
//    }
//
//    public static int rayTraceByDistance(EntityPlayer player, List<AxisAlignedBB> aabbs)
//    {
//        Vec3d posPlayer = player.getPositionEyes(1);
//        List<AxisAlignedBB> sorted = Lists.newArrayList(aabbs);
//        sorted.sort(Comparator.comparingDouble(o -> getCenter(o).squareDistanceTo(posPlayer)));
//        for (AxisAlignedBB aabb : sorted)
//        {
//            if (rayTrace(player, aabb) != null)
//            {
//                return aabbs.indexOf(aabb);
//            }
//        }
//        return -1;
//    }
//
//    public static RayTraceResult rayTrace(EntityPlayer player, AxisAlignedBB aabb)
//    {
//        Vec3d posPlayer = player.getPositionEyes(1);
//        double distance = player.getEntityAttribute(EntityPlayer.REACH_DISTANCE).getAttributeValue();
//        Vec3d posEnd = posPlayer.add(player.getLookVec().scale(distance));
//        return aabb.calculateIntercept(posPlayer, posEnd);
//    }
//
//    public static Vec3d getCenter(AxisAlignedBB aabb)
//    {
//        return new Vec3d(aabb.minX + (aabb.maxX - aabb.minX) * 0.5D, aabb.minY + (aabb.maxY - aabb.minY) * 0.5D, aabb.minZ + (aabb.maxZ - aabb.minZ) * 0.5D);
//    }
//}

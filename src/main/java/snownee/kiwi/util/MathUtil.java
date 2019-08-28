package snownee.kiwi.util;

import java.util.Collection;
import java.util.List;

import com.google.common.collect.Lists;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public class MathUtil
{
    private MathUtil()
    {
    }

    /**
     * https://stackoverflow.com/questions/9600801/evenly-distributing-n-points-on-a-sphere/26127012#26127012
     */
    public static List<Vec3d> fibonacciSphere(Vec3d start, double radius, int samples, boolean randomize)
    {
        double rnd = 1;
        if (randomize)
            rnd = Math.random() * samples;
        double offset = 2d / samples;
        double increment = Math.PI * (3 - Math.sqrt(5));
        List<Vec3d> points = Lists.newArrayListWithCapacity(samples);
        for (int i = 0; i < samples; i++)
        {
            double y = ((i * offset) - 1) + (offset / 2);
            double r = Math.sqrt(1 - y * y) * radius;
            double phi = ((i + rnd) % samples) * increment;
            double x = Math.cos(phi) * r;
            double z = Math.sin(phi) * r;
            points.add(new Vec3d(start.x + x, start.y + y * radius, start.z + z));
        }
        return points;
    }

    public static int posOnLine(Vec3d start, Vec3d end, Collection<BlockPos> list)
    {
        list.add(new BlockPos(start));
        if (start.equals(end))
        {
            return 1;
        }
        else
        {
            int c = 1;
            double ex = MathHelper.lerp(-1.0E-7D, end.x, start.x);
            double ey = MathHelper.lerp(-1.0E-7D, end.y, start.y);
            double ez = MathHelper.lerp(-1.0E-7D, end.z, start.z);
            double sx = MathHelper.lerp(-1.0E-7D, start.x, end.x);
            double sy = MathHelper.lerp(-1.0E-7D, start.y, end.y);
            double sz = MathHelper.lerp(-1.0E-7D, start.z, end.z);
            int x = MathHelper.floor(sx);
            int y = MathHelper.floor(sy);
            int z = MathHelper.floor(sz);
            BlockPos.MutableBlockPos mutableblockpos = new BlockPos.MutableBlockPos(x, y, z);

            double subX = ex - sx;
            double subY = ey - sy;
            double subZ = ez - sz;
            int signX = MathHelper.signum(subX);
            int signY = MathHelper.signum(subY);
            int signZ = MathHelper.signum(subZ);
            double d9 = signX == 0 ? Double.MAX_VALUE : signX / subX;
            double d10 = signY == 0 ? Double.MAX_VALUE : signY / subY;
            double d11 = signZ == 0 ? Double.MAX_VALUE : signZ / subZ;
            double d12 = d9 * (signX > 0 ? 1.0D - MathHelper.frac(sx) : MathHelper.frac(sx));
            double d13 = d10 * (signY > 0 ? 1.0D - MathHelper.frac(sy) : MathHelper.frac(sy));
            double d14 = d11 * (signZ > 0 ? 1.0D - MathHelper.frac(sz) : MathHelper.frac(sz));

            while (d12 <= 1.0D || d13 <= 1.0D || d14 <= 1.0D)
            {
                if (d12 < d13)
                {
                    if (d12 < d14)
                    {
                        x += signX;
                        d12 += d9;
                    }
                    else
                    {
                        z += signZ;
                        d14 += d11;
                    }
                }
                else if (d13 < d14)
                {
                    y += signY;
                    d13 += d10;
                }
                else
                {
                    z += signZ;
                    d14 += d11;
                }

                list.add(new BlockPos(x, y, z));
                ++c;
            }

            return c;
        }
    }
}

package snownee.kiwi.util;

import java.util.List;

import com.google.common.collect.Lists;

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
}

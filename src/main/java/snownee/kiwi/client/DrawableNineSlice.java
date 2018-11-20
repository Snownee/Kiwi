package snownee.kiwi.client;

public class DrawableNineSlice
{
    public int u;
    public int v;
    public int width;
    public int height;
    public int x1;
    public int x2;
    public int y1;
    public int y2;

    public DrawableNineSlice(int u, int v, int x1, int y1, int x2, int y2, int width, int height)
    {
        this.u = u;
        this.v = v;
        this.width = width;
        this.height = height;
        this.x1 = x1;
        this.x2 = x2;
        this.y1 = y1;
        this.y2 = y2;
    }

    public void draw(int x, int y, int panelWidth, int panelHeight, boolean flipX, boolean flipY)
    {
        // flipY not completely implemented
        int centerWidth = panelWidth - x1 - width + x2;
        int centerHeight = panelHeight - y1 - height + y2;

        if (!flipX)
        {
            RenderUtil.drawFlippedModalRect(x, y, x1, y1, u, v, flipX, flipY, 256, 256);
            RenderUtil.drawRepeatedModalRect(x, y + y1, x1, centerHeight, u, v + y1, x1, y2 - y1, flipX, flipY);
            RenderUtil.drawFlippedModalRect(x, y + y1 + centerHeight, x1, height - y2, u, v + y2, flipX, flipY, 256, 256);
        }
        else
        {
            RenderUtil.drawFlippedModalRect(x + x1 + centerWidth, y, width - x2, y1, u, v, flipX, flipY, 256, 256);
            RenderUtil.drawRepeatedModalRect(x + x1 + centerWidth, y + y1, width - x2, centerHeight, u, v + y1, width - x2, y2 - y1, flipX, flipY);
            RenderUtil.drawFlippedModalRect(x + x1 + centerWidth, y + y1 + centerHeight, width - x2, height - y2, u, v + y2, flipX, flipY, 256, 256);
        }

        RenderUtil.drawRepeatedModalRect(x + x1, y, centerWidth, y1, u + x1, v, x2 - x1, y1, flipX, flipY);
        RenderUtil.drawRepeatedModalRect(x + x1, y + y1, centerWidth, centerHeight, u + x1, v + y1, x2 - x1, y2 - y1, flipX, flipY);
        RenderUtil.drawRepeatedModalRect(x + x1, y + y1 + centerHeight, centerWidth, height - y2, u + x1, v + y2, x2 - x1, height - y2, flipX, flipY);

        if (flipX)
        {
            RenderUtil.drawFlippedModalRect(x, y, x1, y1, u + x2, v, flipX, flipY, 256, 256);
            RenderUtil.drawRepeatedModalRect(x, y + y1, x1, centerHeight, u + x2, v + y1, x1, y2 - y1, flipX, flipY);
            RenderUtil.drawFlippedModalRect(x, y + y1 + centerHeight, x1, height - y2, u + x2, v + y2, flipX, flipY, 256, 256);
        }
        else
        {
            RenderUtil.drawFlippedModalRect(x + x1 + centerWidth, y, width - x2, y1, u + x2, v, flipX, flipY, 256, 256);
            RenderUtil.drawRepeatedModalRect(x + x1 + centerWidth, y + y1, width - x2, centerHeight, u + x2, v + y1, width - x2, y2 - y1, flipX, flipY);
            RenderUtil.drawFlippedModalRect(x + x1 + centerWidth, y + y1 + centerHeight, width - x2, height - y2, u + x2, v + y2, flipX, flipY, 256, 256);
        }
    }
}

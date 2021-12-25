package com.zuxelus.energycontrol.renderers;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Matrix3f;
import net.minecraft.util.math.Matrix4f;
import net.minecraft.util.math.Vec3f;
import net.minecraft.util.math.Vector4f;

public class CubeRenderer { // net.minecraft.client.model.geom.ModelPart
	private ModelBox cube;

	public CubeRenderer(int faceOffsetX, int faceOffsetY) {
		this(0.0F, 0.0F, 0.0F, 32, 32, 32, 128, 192, faceOffsetX, faceOffsetY);
	}

	public CubeRenderer(float offX, float offY, float offZ, int width, int height, int depth, float textureWidth, float textureHeight, int faceOffsetX, int faceOffsetY) {
		this(offX, offY, offZ, width, height, depth, textureWidth, textureHeight, faceOffsetX, faceOffsetY, new RotationOffset());
	}

	public CubeRenderer(int faceOffsetX, int faceOffsetY, RotationOffset offset) {
		this(0.0F, 0.0F, 0.0F, 32, 32, 32, 128, 192, faceOffsetX, faceOffsetY, offset);
	}

	public CubeRenderer(float x, float y, float z, int dx, int dy, int dz, float textureWidth, float textureHeight, int faceTexU, int faceTexV, RotationOffset offset) {
		cube = new ModelBox(faceTexU, faceTexV, x, y, z, dx, dy, dz, textureWidth, textureHeight, offset.leftTop, offset.leftBottom, offset.rightTop, offset.rightBottom);
	}

	@Environment(EnvType.CLIENT)
	public void render(MatrixStack matrixStack, VertexConsumer buffer, int[] light, int combinedOverlay) {
		cube.render(matrixStack, buffer, light, combinedOverlay);
	}

	@Environment(EnvType.CLIENT)
	static class PositionTextureVertex {
		public final Vec3f position;
		public final float textureU;
		public final float textureV;

		public PositionTextureVertex(float x, float y, float z, float texU, float texV) {
			this(new Vec3f(x, y, z), texU, texV);
		}

		public PositionTextureVertex setTextureUV(float texU, float texV) {
			return new PositionTextureVertex(this.position, texU, texV);
		}

		public PositionTextureVertex(Vec3f posIn, float texU, float texV) {
			this.position = posIn;
			this.textureU = texU;
			this.textureV = texV;
		}
	}

	@Environment(EnvType.CLIENT)
	static class TexturedQuad {
		public final PositionTextureVertex[] vertexPositions;
		public final Vec3f normal;

		public TexturedQuad(PositionTextureVertex[] positionsIn, float u1, float v1, float u2, float v2, float texWidth, float texHeight, Direction direction) {
			this.vertexPositions = positionsIn;
			float f = 0.0F / texWidth;
			float f1 = 0.0F / texHeight;
			positionsIn[0] = positionsIn[0].setTextureUV(u2 / texWidth - f, v1 / texHeight + f1);
			positionsIn[1] = positionsIn[1].setTextureUV(u1 / texWidth + f, v1 / texHeight + f1);
			positionsIn[2] = positionsIn[2].setTextureUV(u1 / texWidth + f, v2 / texHeight - f1);
			positionsIn[3] = positionsIn[3].setTextureUV(u2 / texWidth - f, v2 / texHeight - f1);
			this.normal = direction.getUnitVector();
		}
	}

	@Environment(EnvType.CLIENT)
	public class ModelBox {
		private final TexturedQuad[] quads;

		public ModelBox(int texOffX, int texOffY, float x, float y, float z, float dx, float dy, float dz, float texWidth, float texHeight, float leftTop, float leftBottom, float rightTop, float rightBottom) {
			quads = new TexturedQuad[6];
			float f = x + dx;
			float f1 = y + dy;
			float f2 = z + dz;
			PositionTextureVertex vertex = new PositionTextureVertex(x, y, z, 0.0F, 0.0F);
			PositionTextureVertex vertex2 = new PositionTextureVertex(f, y, z, 0.0F, 8.0F);
			PositionTextureVertex vertex3 = new PositionTextureVertex(f, f1 - leftTop, z, 8.0F, 8.0F);
			PositionTextureVertex vertex4 = new PositionTextureVertex(x, f1 - leftBottom, z, 8.0F, 0.0F);
			PositionTextureVertex vertex5 = new PositionTextureVertex(x, y, f2, 0.0F, 0.0F);
			PositionTextureVertex vertex6 = new PositionTextureVertex(f, y, f2, 0.0F, 8.0F);
			PositionTextureVertex vertex7 = new PositionTextureVertex(f, f1 - rightTop, f2, 8.0F, 8.0F);
			PositionTextureVertex vertex8 = new PositionTextureVertex(x, f1 - rightBottom, f2, 8.0F, 0.0F);
			quads[2] = new TexturedQuad(new PositionTextureVertex[] { vertex6, vertex5, vertex, vertex2 }, dz, 0, dz + dx, dz, texWidth, texHeight, Direction.DOWN);
			quads[3] = new TexturedQuad(new PositionTextureVertex[] { vertex3, vertex4, vertex8, vertex7 }, texOffX + dz + dx, texOffY + dz, texOffX + dz + dx + dx, texOffY, texWidth, texHeight, Direction.UP);
			quads[1] = new TexturedQuad(new PositionTextureVertex[] { vertex, vertex5, vertex8, vertex4 }, 0, dz, dz, dz + dy, texWidth, texHeight, Direction.WEST);
			quads[4] = new TexturedQuad(new PositionTextureVertex[] { vertex2, vertex, vertex4, vertex3 }, dz, dz, dz + dx, dz + dy, texWidth, texHeight, Direction.NORTH);
			quads[0] = new TexturedQuad(new PositionTextureVertex[] { vertex6, vertex2, vertex3, vertex7 }, dz + dx, dz, dz + dx + dz, dz + dy, texWidth, texHeight, Direction.EAST);
			quads[5] = new TexturedQuad(new PositionTextureVertex[] { vertex5, vertex6, vertex7, vertex8 }, dz + dx + dz, dz, dz + dx + dz + dx, dz + dy, texWidth, texHeight, Direction.SOUTH);
		}

		public void render(MatrixStack matrixStack, VertexConsumer buffer, int[] light, int combinedOverlay) {
			matrixStack.scale(0.5F, 0.5F, 0.5F);
			render(matrixStack.peek(), buffer, light, combinedOverlay, 1.0F, 1.0F, 1.0F, 1.0F);
			matrixStack.scale(2.0F, 2.0F, 2.0F);
		}

		public void render(MatrixStack.Entry matrixEntry, VertexConsumer buffer, int[] light, int combinedOverlay, float red, float green, float blue, float alpha) {
			Matrix4f matrix4f = matrixEntry.getPositionMatrix();
			Matrix3f matrix3f = matrixEntry.getNormalMatrix();

			for (int n = 0; n < quads.length; ++n) {
				TexturedQuad quad = quads[n];
				Vec3f vector3f = quad.normal.copy();
				vector3f.transform(matrix3f);
				float f = vector3f.getX();
				float g = vector3f.getY();
				float h = vector3f.getZ();

				for (int i = 0; i < 4; ++i) {
					PositionTextureVertex vertex = quad.vertexPositions[i];
					Vector4f vector4f = new Vector4f(vertex.position.getX() / 16.0F, vertex.position.getY() / 16.0F, vertex.position.getZ() / 16.0F, 1.0F);
					vector4f.transform(matrix4f);
					buffer.vertex(vector4f.getX(), vector4f.getY(), vector4f.getZ(), red, green, blue, alpha, vertex.textureU, vertex.textureV, combinedOverlay, light[n], f, g, h);
				}
			}
		}
	}
}
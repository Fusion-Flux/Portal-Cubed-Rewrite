package io.github.fusionflux.portalcubed.framework.extension;

/**
 * Marker interface for blocks to opt into better collision checking when raycasting.
 * Normally, block shapes extending beyond their 1x1x1 are ignored. When this interface is
 * present, blocks can have collision shapes up to 3x3x3 in size.
 */
public interface BigShapeBlock {
}

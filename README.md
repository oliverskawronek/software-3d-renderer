# Software 3D Renderer
A simple OpenGL like software renderer written in BlitzBasic.

**Highlights**:
* Perspective projection
* Z-Buffering
* Supported Primitives: `SR_POINTS`, `SR_LINES`, `SR_TRIANGLES`
* Transformation: `srRotate`, `srTranslate`, `srScale`
* Lightning: `SR_LIGHT1` (pointlight) is alway turned on.
* Shading: Flat shading (vertex normals currently ignored)

# How to use
Copy `renderer.bb` and `math.bb` into you project's include folder and type `include "renderer.bb"`. Switch to the graphics mode by call the `Graphics` command. Every call to the software renderer must made between `srInit` and `srDestroy`. Don't forget to call `Flip` every frame.

# Screenshot
**Flat Shading**
![Screenshot](https://cloud.githubusercontent.com/assets/10528519/8034901/8b73ff3e-0dec-11e5-9bfa-b3ad9e228749.png "Screenshot showing Flat Shading")

# Example
Example taken from _triangle-example.bb_. Renders a filled triangle.

```BlitzBasic
Include "renderer.bb"

Graphics(800, 600, 0, 2)
SetBuffer BackBuffer()

srInit()

srMatrixMode(SR_PROJECTION)
srLoadIdentity()
srPerspective(1.0, Float(GraphicsWidth())/Float(GraphicsHeight()), 0.1, 100.0)
srDepthRange(0.0, 100.0)

srMatrixMode(SR_MODELVIEW)

While Not KeyDown(1)
	Cls()
	srLoadIdentity()
	srTranslate(0.0, 0.0, -2)
	
	srBegin(SR_TRIANGLES)
	srVertex( 0,  1, 0) ; Top Middle
	srVertex(-1, -1, 0) ; Bottom Left
	srVertex( 1, -1, 0) ; Bottom Right
	srEnd()

	Flip(0)
Wend

srDestroy()
End
```

# Rendering pipeline
Rendering is allmost done in OpenGL style:

1. Eye coordinates = ModelView x Object coordinates
2. Eye normal = Inverse ModelView x Object normal coordinates
3. Clip coordinates = Projection matrix x Eye coordinates
4. Normalized device coordinates = Clip coordinates x, y, z / Clip w
5. View transformation
 1. TempOriginX = Viewport width/2 + Viewport x
 2. TempOriginY = Viewport height/2 + Viewport y
 3. TempRangeHalf = (Depth range far - Depth range near)/2
 4. Window X = Viewport width/2 * Device x + TempOriginX
 5. Window Y = Viewport height/2 * Device y + TempOriginY
 6. Window Z = TempRangeHalf*Device z + TempRangeHalf
7. Clear Z Buffer
6. Draw primitives in window coordinates. (Depth function is always set to GL_GREATER)

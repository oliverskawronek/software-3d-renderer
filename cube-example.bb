Include "renderer.bb"

Global Angle#, Z#

Graphics(800, 600, 0, 2)
SetBuffer BackBuffer()

srInit()

srMatrixMode(SR_PROJECTION)
srLoadIdentity()
srPerspective(1.0, Float(GraphicsWidth())/Float(GraphicsHeight()), 0.1, 100.0)
srDepthRange(0.0, 100.0)

srMatrixMode(SR_MODELVIEW)
Z = -5.0

srLight(SR_LIGHT1, 0, 10, -10)
While Not KeyDown(1)
	If KeyDown(200) Then Z = Z + 0.5
	If KeyDown(208) Then Z = Z - 0.5
	Angle = Angle + 1.0

	Cls()
	srLoadIdentity()
	srRotate(Angle, 1, 0, 0)
	srRotate(Angle, 0, 1, 0)
	srTranslate(0.0, 0.0, Z)
	RenderCube()

	Flip(0)
Wend

End

Function RenderCube()
	srBegin(SR_TRIANGLES)
	
	; Front
	srNormal(0, 0, -1)
	srVertex(-1,  1, -1)
	srVertex( 1,  1, -1)
	srVertex( 1, -1, -1)
	srVertex( 1, -1, -1)
	srVertex(-1, -1, -1)
	srVertex(-1,  1, -1)
	
	; Back
	srNormal(0, 0, 1)
	srVertex(-1,  1, 1)
	srVertex( 1, -1, 1)
	srVertex( 1,  1, 1)
	srVertex( 1, -1, 1)
	srVertex(-1,  1, 1)
	srVertex(-1, -1, 1)
	
	; Left
	srNormal(-1, 0, 0)
	srVertex(-1,  1, -1)
	srVertex(-1, -1, -1)
	srVertex(-1,  1,  1)
	srVertex(-1,  1,  1)
	srVertex(-1, -1, -1)
	srVertex(-1, -1,  1)

	; Right
	srNormal(1, 0, 0)
	srVertex(1,  1, -1)
	srVertex(1,  1,  1)
	srVertex(1, -1, -1)
	srVertex(1,  1,  1)
	srVertex(1, -1,  1)
	srVertex(1, -1, -1)

	; Top
	srNormal(0, 1, 0)
	srVertex(-1, 1, -1)
	srVertex( 1, 1,  1)
	srVertex( 1, 1, -1)
	srVertex(-1, 1, -1)
	srVertex(-1, 1,  1)
	srVertex( 1, 1,  1)

	; Bottom
	srNormal(0, -1, 0)
	srVertex(-1, -1, -1)
	srVertex( 1, -1, -1)
	srVertex( 1, -1,  1)
	srVertex(-1, -1, -1)
	srVertex( 1, -1,  1)
	srVertex(-1, -1,  1)

	srEnd()
End Function
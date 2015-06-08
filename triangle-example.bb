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

End

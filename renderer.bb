Include "math.bb"

; - Constants / Globals ------------------------------------
; Matrices
Const SR_MODELVIEW  = 1
Const SR_PROJECTION = 2

; Primitives
Const SR_POINTS    = 1
Const SR_LINES     = 2
Const SR_TRIANGLES = 3
Global gPrimitive

; Light
Const SR_LIGHT_AMBIENT = 1
Const SR_LIGHT1 = 2
Global gLight

; Depthrange Far / Near
Global gDepthRange#[1]

; Viewport X, Y, Width, Height
Global gViewport[3]

; Render States
Const SR_RENDERING = %1
Global gRenderState

Type TVertexBuffer
	Field Colour.TVector
	Field Position.TVector
	Field Normal.TVector
End Type

; Matrices
Global gModelView.TMatrix
Global gModelViewIT.TMatrix
Global gProjection.TMatrix
Global gMatrixMode
Global gActMatrix.TMatrix

; Vertex Pipeline
Global gLightAmbient.TVector
Global gLight1.TVector
Global gColour.TVector
Global gNormal.TVector
Global gObject.TVector
Global gEye.TVector
Global gEyeNormal.TVector
Global gClip.TVector
Global gDevice.TVector
Global gWindow.TVector

; Temp
Global gTmpVector.TVector
Global gTmpMatrix.TMatrix
Global gTmpMatrix2.TMatrix

Global gTmpEdge1.TVector
Global gTmpEdge2.TVector
Global gTmpNormal.TVector

Global gTmpEye1.TVector
Global gTmpEye2.TVector
Global gTmpEye3.TVector

Dim gZBuffer#(1024,1024)

; - API ----------------------------------------------------
Function srInit()
	gLight = SR_LIGHT1
	
	gLightAmbient = New TVector
	gLight1 = New TVector

	gDepthRange[0] = 0.0
	gDepthRange[1] = 1.0

	gViewport[0] = 0
	gViewport[1] = 0
	gViewport[2] = GraphicsWidth()
	gViewport[3] = GraphicsHeight()

	gModelView   = New TMatrix
	gModelViewIT = New TMatrix
	gProjection  = New TMatrix
	gMatrixMode  = SR_MODELVIEW
	gActMatrix   = gModelView

	gColour    = New TVector
	gNormal    = New TVector
	gObject    = New TVector
	gEye       = New TVector
	gEyeNormal = New TVector
	gClip      = New TVector
	gDevice    = New TVector
	gWindow    = New TVector

	gTmpVector  = New TVector
	gTmpMatrix  = New TMatrix
	gTmpMatrix2 = New TMatrix
	gTmpEdge1   = New TVector
	gTmpEdge2   = New TVector
	gTmpNormal  = New TVector
	gTmpEye1    = New TVector
	gTmpEye2    = New TVector
	gTmpEye3    = New TVector
End Function

Function srDestroy()
	Delete gModelView
	Delete gModelViewIT
	Delete gProjection
	Delete gActMatrix

	Delete gColour
	Delete gNormal
	Delete gObject
	Delete gEye
	Delete gEyeNormal
	Delete gDevice
	Delete gWindow

	Delete gTmpVector
	Delete gTmpMatrix
	Delete gTmpMatrix2
	
	Delete gTmpEdge1
	Delete gTmpEdge2
	Delete gTmpNormal
	
	Delete gTmpEye1
	Delete gTmpEye2
	Delete gTmpEye3
	
	If gRenderState = SR_RENDERING Then UnlockBuffer()
End Function

Function srDepthRange(Near#, Far#)
	If Near < 0.0 Then Near = 0.0
	If Near > 1.0 Then Near = 1.0
	gDepthRange[0] = Near

	If Far < 0.0 Then Far = 0.0
	If Far > 1.0 Then Far = 1.0
	gDepthRange[1] = Far
End Function

Function srViewport(X, Y, Width, Height)
	gViewport[0] = X
	gViewport[1] = X
	gViewport[2] = Width
	gViewport[3] = Height
End Function

Function srMatrixMode(Mode)
	Select Mode
		Case SR_MODELVIEW
			gActMatrix = gModelView
		
		Case SR_PROJECTION
			gActMatrix = gProjection
		
		Default
			RuntimeError("Matrix mode does not exist")
	End Select

	gMatrixMode = Mode
End Function

Function srLoadIdentity()
	MatrixIdentity(gActMatrix)
	If gMatrixMode = SR_MODELVIEW Then MatrixIdentity(gModelViewIT)
End Function

Function srRotate(Angle#, X#, Y#, Z#)
	MatrixRotate(gTmpMatrix2, Angle, X, Y, Z)
	MatrixMatrixMultiply(gTmpMatrix2, gActMatrix, gActMatrix)
	If gMatrixMode = SR_MODELVIEW Then
		MatrixCopy(gTmpMatrix2, gTmpMatrix)
		MatrixTranspose(gTmpMatrix, gTmpMatrix2)
		MatrixMatrixMultiply(gTmpMatrix2, gModelViewIT, gModelViewIT)
	EndIf
End Function

Function srTranslate(X#, Y#, Z#)
	MatrixTranslate(gTmpMatrix2, X, Y, Z)
	MatrixMatrixMultiply(gTmpMatrix2, gActMatrix, gActMatrix)
	If gMatrixMode = SR_MODELVIEW Then
		MatrixTranslate(gTmpMatrix2, -X, -Y, -Z)
		MatrixMatrixMultiply(gTmpMatrix2, gModelViewIT, gModelViewIT)
	EndIf
End Function

Function srScale(X#, Y#, Z#)
	MatrixScale(gTmpMatrix2, X, Y, Z)
	MatrixMatrixMultiply(gTmpMatrix2, gActMatrix, gActMatrix)
	If gMatrixMode = SR_MODELVIEW Then
		MatrixScale(gTmpMatrix2, 1.0/X, 1.0/Y, 1.0/Z)
		MatrixMatrixMultiply(gTmpMatrix2, gModelViewIT, gModelViewIT)
	EndIf
End Function

Function srPerspective(Zoom#, Aspect#, Far#, Near#)
	MatrixPerspective(gTmpMatrix2, Zoom, Aspect, Far, Near)
	MatrixMatrixMultiply(gTmpMatrix2, gActMatrix, gActMatrix)
	If gMatrixMode = SR_MODELVIEW Then srError("Unable to invert Projection")
End Function

Function srNormal(X#, Y#, Z#, W#=1.0)
	gNormal\X = X
	gNormal\Y = Y
	gNormal\Z = Z
	gNormal\W = W
	VectorNormalize(gNormal)
End Function

Function srVertex(X#, Y#, Z#, W#=1.0)
	Local vertex.TVertexBuffer = New TVertexBuffer

	vertex\Position = New TVector
	vertex\Position\X = X
	vertex\Position\Y = Y
	vertex\Position\Z = Z
	vertex\Position\W = W#
	vertex\Normal = New TVector
	VectorCopy(gNormal, vertex\Normal)
	vertex\Colour = New TVector
	VectorCopy(gColour, vertex\Colour)
End Function

Function srLight(Light%, X#, Y#, Z#)
	If Light = SR_LIGHT_AMBIENT Then
		gLightAmbient\X# = X#
		gLightAmbient\Y# = Y#
		gLightAmbient\Z# = Z#
	ElseIf Light = SR_LIGHT1 Then
		gLight1\X# = X#
		gLight1\Y# = Y#
		gLight1\Z# = Z#
	EndIf
End Function

Function srVertexProcessor()
	Local OX, OY

	; Eye = MV x Object
	MatrixVectorMultiply(gModelView, gObject, gEye)
	
	; Normal Eye = (MV^-1)^T*gNormal
	MatrixVectorMultiply(gModelViewIT, gNormal, gEyeNormal)
	;VectorCopy(gNormal, gEyeNormal)

	; Clip = P x Eye
	MatrixVectorMultiply(gProjection, gEye, gClip)

	; Normalized Device Coords
	gDevice\X = gClip\X/gClip\W
	gDevice\Y = gClip\Y/gClip\W
	gDevice\Z = gClip\Z/gClip\W
	
	; Viewport transformation	
	OX = gViewport[2]/2 + gViewport[0]
	OY = gViewport[3]/2 + gViewport[1]

	gWindow\X = (gViewport[2]/2.0)*gDevice\X + OX
	gWindow\Y = (gViewport[3]/2.0)*gDevice\Y + OY
	gWindow\Z = ((gDepthRange[1] - gDepthRange[0])/2.0)*gDevice\Z + ((gDepthRange[1] + gDepthRange[0])/2.0)
End Function

Function srBegin(primitive)
	Local x, y

	If gRenderState And SR_RENDERING <> 0 Then srError("Unexpected Render State")
	
	gPrimitive = primitive
	gRenderState = gRenderState Or SR_RENDERING
	
	MatrixCopy(gTmpMatrix, gModelViewIT)
	MatrixTranspose(gModelViewIT, gTmpMatrix)
	srDebugMatrix(gModelViewIT)
	
	For x = 0 To gViewport[2]
		For y = 0 To gViewport[3]
			gZBuffer(x, y) = -1.0
		Next
	Next
	
	LockBuffer()
End Function

Function srEnd()
	Local v1.TVertexBuffer, v2.TVertexBuffer, v3.TVertexBuffer
	Local w1x#, w1y#, w1z#
	Local w2x#, w2y#, w2z#
	Local w3x#, w3y#, w3z#
	Local viewAngle#, lightAngle#

	If gRenderState And SR_RENDERING = 0 Then srError("Unexpected Render State")

	; Do rendering
	Select gPrimitive
		Case SR_POINTS
			v1 = First TVertexBuffer
			While v1 <> Null
				; Vertex 1
				srPushVertex(v1)
				srVertexProcessor()
				w1x# = gWindow\X
				w1y# = gWindow\Y
				w1z# = gWindow\Z
				
				srDrawPoint(w1x#, w1y#, w1z#, $FFFFFF)

				v1 = After v1
			Wend
		Case SR_LINES
			v1 = First TVertexBuffer
			If v1 <> Null Then
				v2 = After v1
			Else
				v2 = Null
			EndIf
						
			While v1 <> Null
				If (v1 <> Null) And (v2 = Null) Then srError("Incorrect Number of Vertices")
				
				; Vertex 1
				srPushVertex(v1)
				srVertexProcessor()
				w1x# = gWindow\X
				w1y# = gWindow\Y
				w1z# = gWindow\Z
				
				; Vertex 2
				srPushVertex(v2)
				srVertexProcessor()
				w2x# = gWindow\X
				w2y# = gWindow\Y
				w2z# = gWindow\Z
				
				srDrawLine(w1x#, w1y#, w1z#, w2x#, w2y#, w2z#, $FFFFFF, $FFFFFF)
				
				v1 = After v2
				If v1 <> Null Then
					v2 = After v1
				Else
					v2 = Null
				EndIf
			Wend
	
		Case SR_TRIANGLES
			v1 = First TVertexBuffer
			If v1 <> Null Then
				v2 = After v1
				If v2 <> Null Then
					v3 = After v2
				Else
					v3 = Null
				EndIf
			Else
				v2 = Null
			EndIf
						
			While v1 <> Null
				If ((v1 <> Null) And (v2 = Null Or v3 = Null)) Then srError("Incorrect Number of Vertices")
				
				; Vertex 1
				srPushVertex(v1)
				srVertexProcessor()
				VectorCopy(gEye, gTmpEye1)
				w1x# = gWindow\X
				w1y# = gWindow\Y
				w1z# = gWindow\Z
					
				; Vertex 2
				srPushVertex(v2)
				srVertexProcessor()
				VectorCopy(gEye, gTmpEye2)
				w2x# = gWindow\X
				w2y# = gWindow\Y
				w2z# = gWindow\Z
	
				; Vertex 3
				srPushVertex(v3)
				srVertexProcessor()
				VectorCopy(gEye, gTmpEye3)
				w3x# = gWindow\X
				w3y# = gWindow\Y
				w3z# = gWindow\Z
				
				; Triangle Normal
				VectorSubstract(gTmpEye1, gTmpEye2, gTmpEdge1)
				VectorSubstract(gTmpEye1, gTmpEye3, gTmpEdge2)
				VectorCrossProduct(gTmpEdge1, gTmpEdge2, gTmpNormal)
				VectorNormalize(gTmpNormal)

				gTmpVector\X# = 0.0
				gTmpVector\Y# = 0.0
				gTmpVector\Z# = -1.0
				viewAngle# = VectorDotProduct(gTmpVector, gTmpNormal)
				
				If viewAngle < 0.0 Then
					lightAngle = -VectorDotProduct(gEyeNormal, gLight1)/(VectorLength(gEyeNormal)*VectorLength(gLight1))
					cc = -viewAngle * 255
					If cc < 0 Then cc = 0
					If cc > 255 Then cc = 255
					ccc = (cc Shl 16) Or (cc Shl 8) Or cc
					
					srDrawTriangle(w1x#, w1y#, -w1z#, w2x#, w2y#, -w2z#, w3x#, w3y#, -w3z#, ccc, ccc, ccc)
				EndIf
				
				v1 = After v3
				If v1 <> Null Then
					v2 = After v1
					If v2 <> Null Then
						v3 = After v2
					Else
						v3 = Null
					EndIf
				Else
					v2 = Null
				EndIf
			Wend
	End Select
	
	gRenderState = gRenderState And (~SR_RENDERING)
	For Vertex.TVertexBuffer = Each TVertexBuffer
		srDeleteVertex(Vertex)
	Next
	Delete Each TVertexBuffer
	
	UnlockBuffer()
End Function

; Internal Functions ---------------------------------------
Function srDrawPoint(X#, Y#, Z#, Colour%)
	; (x=0, y=0): left, bottom 
	
	X# = Int(X)
	Y# = gViewport[3] - Int(Y) ; set y=0 to bottom 
	If X < gViewport[0] Or X >= gViewport[0] + gViewport[2] Or Y < gViewport[1] Or Y >= gViewport[1] + gViewport[3] Then Return
	If z <= gZBuffer(X, Y) Then Return
	WritePixelFast X#, Y#, Colour
	gZBuffer(X, Y) = Z
End Function

Function srDrawLine(X1#, Y1#, Z1#, X2#, Y2#, Z2#, Colour1%, Colour2%)
	Local x1d = x1, y1d = y1 ; discrete of (x1, y1)
	Local x2d = x2, y2d = y2 ; discrete of (x2, y2)

	Local swap = Abs(y2d - y1d) > Abs(x2d - x1d)
	If swap Then
		Local tmp
		
		; swap x1d, y1d
		tmp = x1d
		x1d = y1d
		y1d = tmp
		
		; swap x2d, y2d
		tmp = x2d
		x2d = y2d
		y2d = tmp
	EndIf
	
	Local deltaX = Abs(x2d - x1d)
	Local deltaY = Abs(y2d - y1d)
	Local err = 0
	
	Local x = x1d, y = y1d
	Local xStep, yStep
	Local deltaErr = deltaY
	
	If x1d < x2d Then
		xStep = 1
	Else
		xStep = -1
	EndIf
	
	If y1d < y2d Then
		yStep = 1
	Else
		yStep = -1
	EndIf

	If Not swap Then
		srDrawPoint(x, y, Z1, Colour1)
	Else
		srDrawPoint(y, x, Z1, Colour1)
	EndIf
	
	While x <> x2d
		x = x + xStep
		err = err + deltaErr
		
		If (err Shl 1) > deltaX Then
			y = y + yStep
			err = err - deltaX
		EndIf
		
		If Not swap Then
			srDrawPoint(x, y, Z1, Colour1)
		Else
			srDrawPoint(y, x, Z1, Colour1)
		EndIf
	Wend
End Function

Function srDrawTriangle(X1#, Y1#, Z1#, X2#, Y2#, Z2#, X3#, Y3#, Z3#, Colour1%, Colour2%, Colour3%)
	Local MaxX = gViewport[0] + gViewport[2]
	Local MaxY = gViewport[1] + gViewport[3]
	Local tx#, ty#, tz#, tc%
	Local tz1#, tz2#, z#
	
	
	If X1 >= gViewport[0] And X1 < MaxX And Y1 >= gViewport[1] And Y1 < MaxY Then
	ElseIf X2 >= gViewport[0] And X2 < MaxX And Y2 >= gViewport[1] And Y2 < MaxY Then
	ElseIf X3 >= gViewport[0] And X3 < MaxX And Y3 >= gViewport[1] And Y3 < MaxY Then
	Else
	Return
	EndIf
	
	If y1# > y3# Then
		tx# = x1#
		ty# = y1#
		tz# = z1#
		tc = Colour1
		x1# = x3#
		y1# = y3#
		z1# = z3#
		x3# = tx#
		y3# = ty#
		z3# = tz#
		Colour1 = Colour3
		Colour3 = tc
	EndIf
	
	If Y1# > Y2# Then
		tx# = x1#
		ty# = y1#
		tz# = z1#
		tc = Colour1
		x1# = x2#
		y1# = y2#
		z1# = z2#
		x2# = tx#
		y2# = ty#
		z2# = tz#
		Colour1 = Colour2
		Colour2 = tc
	EndIf
	
	If y2# > y3# Then
		tx# = x2#
		ty# = y2#
		tz# = z2#
		tc = Colour2
		x2# = x3#
		y2# = y3#
		z2# = z3#
		x3# = tx#
		y3# = ty#
		z3# = tz#
		Colour2 = Colour3
		Colour3 = tc
	EndIf

	x1 = Int(x1)
	x2 = Int(x2)
	x3 = Int(x3)
	y1 = Int(y1)
	y2 = Int(y2)
	y3 = Int(y3)
	
	If y1 <> y2 Then
		For y = y1# To y2#			
			If y >= gViewport[1] And y < gViewport[1] + gViewport[3] Then
				xl   = x1# + (x1# - x2#)/(y1# - y2#)*(y - y1#)
				xr   = x1# + (x3# - x1#)/(y3# - y1#)*(y - y1#)
				tz1# = z1# + (z1# - z2#)/(y1# - y2#)*(y - y1#)
				tz2# = z1# + (z3# - z1#)/(y3# - y1#)*(y - y1#)
				
				If xl > xr Then
					x = xr
					xr = xl
					xl = x
				EndIf
				
				If xl < gViewport[0] Then xl = gViewport[0]
				If xr >= gViewport[0] + gViewport[2] Then xr = gViewport[0] + gViewport[2] - 1
				
				
				For x = xl To xr
					z# = tz1# + (tz2# - tz1#)/(xr - xl)*(x - xl)
					srDrawPoint(x, y, z, Colour1)
				Next
			EndIf
		Next
	EndIf
	
	If y2 <> y3 Then
		Color 0, 255, 0
		For y = y2# To y3#
			If y >= gViewport[1] And y < gViewport[1] + gViewport[3] Then
				xl   = x2# + (x2# - x3#)/(y2# - y3#)*(y - y2#)
				xr   = x3# + (x3# - x1#)/(y3# - y1#)*(y - y3#)
				tz1# = z2# + (z2# - z3#)/(y2# - y3#)*(y - y2#)
				tz2# = z3# + (z3# - z1#)/(y3# - y1#)*(y - y3#)
				
				If xl > xr Then
					x = xr
					xr = xl
					xl = x
				EndIf
				
				If xl < gViewport[0] Then xl = gViewport[0]
				If xr >= gViewport[0] + gViewport[2] Then xr = gViewport[0] + gViewport[2] - 1
				
				
				For x = xl To xr
					z# = tz1# + (tz2# - tz1#)/(xr - xl)*(x - xl)
					srDrawPoint(x, y, z, Colour1)
				Next
			EndIf
		Next
	EndIf
End Function

Function srDebugMatrix(M.TMatrix)
	MatrixDebug(M)
End Function

Function srPushVertex(Vertex.TVertexBuffer)
	VectorCopy(Vertex\Position, gObject)
	VectorCopy(Vertex\Normal, gNormal)
	VectorCopy(Vertex\Colour, gColour)
End Function

Function srDeleteVertex(Vertex.TVertexBuffer)
	If Vertex\Position <> Null Then Delete Vertex\Position
	If Vertex\Normal <> Null Then Delete Vertex\Normal
	If Vertex\Colour <> Null Then Delete Vertex\Colour
End Function

Function srError(message$)
	srDestroy()
	Color 255, 255, 255
	Locate 0, 0
	Print message$
	AppTitle message$
	WaitKey()
	End()
End Function
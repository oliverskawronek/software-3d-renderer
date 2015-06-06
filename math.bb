Type TVector
	Field X#
	Field Y#
	Field Z#
	Field W#
End Type

Type TMatrix
	Field AA#, AB#, AC#, AD#
	Field BA#, BB#, BC#, BD#
	Field CA#, CB#, CC#, CD#
	Field DA#, DB#, DC#, DD#
End Type

Function VectorCrossProduct(A.TVector, B.TVector, R.TVector)
	R\X# = A\Y#*B\Z# - A\Z#*B\Y#
	R\Y# = A\Z#*B\X# - A\X#*B\Z#
	R\Z# = A\X#*B\Y# - A\Y#*B\X#
End Function

Function VectorSubstract(A.TVector, B.TVector, R.TVector)
	R\X# = A\X# - B\X#
	R\Y# = A\Y# - B\Y#
	R\Z# = A\Z# - B\Z#
End Function

Function VectorAdd(A.TVector, B.TVector, R.TVector)
	R\X# = A\X# + B\X#
	R\Y# = A\Y# + B\Y#
	R\Z# = A\Z# + B\Z#
End Function

Function VectorCopy(V.TVector, R.TVector)
	R\X# = V\X#
	R\Y# = V\Y#
	R\Z# = V\Z#
	R\W# = V\W#
End Function

Function VectorDotProduct#(A.TVector, B.TVector)
	Return A\X#*B\X# + A\Y#*B\Y# + A\Z#*B\Z#
End Function

Function VectorLength#(V.TVector)
	Return Sqr#(V\X#*V\X# + V\Y#*V\Y# + V\Z#*V\Z#)
End Function

Function VectorNormalize(V.TVector)
	Local length# = VectorLength(V)
	V\X# = V\X# / length#
	V\Y# = V\Y# / length#
	V\Z# = V\Z# / length#
End Function

Function VectorDebug(V.TVector)
	DebugLog V\X# + " " + V\Y# + " " + V\Z# + " " + V\W#
End Function

Function MatrixCopy(M.TMatrix, R.TMatrix)
	R\AA = M\AA : R\AB = M\AB : R\AC = M\AC : R\AD = M\AD
	R\BA = M\BA : R\BB = M\BB : R\BC = M\BC : R\BD = M\BD
	R\CA = M\CA : R\CB = M\CB : R\CC = M\CC : R\CD = M\CD
	R\DA = M\DA : R\DB = M\DB : R\DC = M\DC : R\DD = M\DD
End Function

Function MatrixIdentity(M.TMatrix)
	M\AA = 1.0 : M\AB = 0.0 : M\AC = 0.0  : M\AD = 0.0
	M\BA = 0.0 : M\BB = 1.0 : M\BC = 0.0  : M\BD = 0.0
	M\CA = 0.0 : M\CB = 0.0 : M\CC = 1.0  : M\CD = 0.0
	M\DA = 0.0 : M\DB = 0.0 : M\DC = 0.0  : M\DD = 1.0
End Function

Function MatrixRotate(M.TMatrix, Angle#, X#, Y#, Z#)
	Local C#, S#, RLength#

	C = Cos(Angle)
	S = Sin(Angle)

	; Normalize Vector
	RLength = 1.0/Sqr(X*X + Y*Y + Z*Z)
	X = X*RLength
	Y = Y*RLength
	Z = Z*RLength

	M\AA = X*X*(1.0 - C) + C
	M\AB = X*Y*(1.0 - C) - Z*S
	M\AC = X*Z*(1.0 - C) + Y*S
	M\AD = 0.0

	M\BA = Y*X*(1.0 - C) + Z*S
	M\BB = Y*Y*(1 - C) + C
	M\BC = Y*Z*(1.0 - C) - X*S
	M\BD = 0.0

	M\CA = X*Z*(1.0 - C) - Y*S
	M\CB = Y*Z*(1.0 - C) + X*S
	M\CC = Z*Z*(1.0 - C) + C
	M\CD = 0.0

	M\DA = 0.0
	M\DB = 0.0
	M\DC = 0.0
	M\DD = 1.0
End Function

Function MatrixTranspose(M.TMatrix, R.TMatrix)
	R\AA = M\AA : R\AB = M\BA : R\AC = M\CA : R\AD = M\DA
	R\BA = M\AB : R\BB = M\BB : R\BC = M\CB : R\BD = M\DB
	R\CA = M\AC : R\CB = M\BC : R\CC = M\CC : R\CD = M\DC
	R\DA = M\AD : R\DB = M\BD : R\DC = M\CD : R\DD = M\DD
End Function

Function MatrixTranslate(M.TMatrix, X#, Y#, Z#)
	M\AA = 1.0 : M\AB = 0.0 : M\AC = 0.0  : M\AD = X
	M\BA = 0.0 : M\BB = 1.0 : M\BC = 0.0  : M\BD = Y
	M\CA = 0.0 : M\CB = 0.0 : M\CC = 1.0  : M\CD = Z
	M\DA = 0.0 : M\DB = 0.0 : M\DC = 0.0  : M\DD = 1.0
End Function

Function MatrixScale(M.TMatrix, X#, Y#, Z#)
	M\AA = X   : M\AB = 0.0 : M\AC = 0.0  : M\AD = 0.0
	M\BA = 0.0 : M\BB = Y   : M\BC = 0.0  : M\BD = 0.0
	M\CA = 0.0 : M\CB = 0.0 : M\CC = Z    : M\CD = 0.0
	M\DA = 0.0 : M\DB = 0.0 : M\DC = 0.0  : M\DD = 1.0
End Function

Function MatrixPerspective(M.TMatrix, Zoom#, Aspect#, Near#, Far#)
	M\AA = Zoom/Aspect : M\AB = 0.0  : M\AC = 0.0                       : M\AD = 0.0
	M\BA = 0.0         : M\BB = Zoom : M\BC = 0.0                       : M\BD = 0.0
	M\CA = 0.0         : M\CB = 0.0  : M\CC = (Far + Near)/(Near - Far) : M\CD = (2.0*Far*Near) / (Near - Far)
	M\DA = 0.0         : M\DB = 0.0  : M\DC = -1.0                      : M\DD = 0.0
End Function

Function MatrixDebug(M.TMatrix)
	DebugLog M\AA + " " + M\AB + " " + M\AC + " " + M\AD
	DebugLog M\BA + " " + M\BB + " " + M\BC + " " + M\BD
	DebugLog M\CA + " " + M\CB + " " + M\CC + " " + M\CD
	DebugLog M\DA + " " + M\DB + " " + M\DC + " " + M\DD
End Function

Function MatrixMatrixMultiply(X.TMatrix, Y.TMatrix, R.TMatrix)
	gTmpMatrix\AA = X\AA*Y\AA + X\AB*Y\BA + X\AC*Y\CA + X\AD*Y\DA
	gTmpMatrix\AB = X\AA*Y\AB + X\AB*Y\BB + X\AC*Y\CB + X\AD*Y\DB
	gTmpMatrix\AC = X\AA*Y\AC + X\AB*Y\BC + X\AC*Y\CC + X\AD*Y\DC
	gTmpMatrix\AD = X\AA*Y\AD + X\AB*Y\BD + X\AC*Y\CD + X\AD*Y\DD

	gTmpMatrix\BA = X\BA*Y\AA + X\BB*Y\BA + X\BC*Y\CA + X\BD*Y\DA
	gTmpMatrix\BB = X\BA*Y\AB + X\BB*Y\BB + X\BC*Y\CB + X\BD*Y\DB
	gTmpMatrix\BC = X\BA*Y\AC + X\BB*Y\BC + X\BC*Y\CC + X\BD*Y\DC
	gTmpMatrix\BD = X\BA*Y\AD + X\BB*Y\BD + X\BC*Y\CD + X\BD*Y\DD

	gTmpMatrix\CA = X\CA*Y\AA + X\CB*Y\BA + X\CC*Y\CA + X\CD*Y\DA
	gTmpMatrix\CB = X\CA*Y\AB + X\CB*Y\BB + X\CC*Y\CB + X\CD*Y\DB
	gTmpMatrix\CC = X\CA*Y\AC + X\CB*Y\BC + X\CC*Y\CC + X\CD*Y\DC
	gTmpMatrix\CD = X\CA*Y\AD + X\CB*Y\BD + X\CC*Y\CD + X\CD*Y\DD

	gTmpMatrix\DA = X\DA*Y\AA + X\DB*Y\BA + X\DC*Y\CA + X\DD*Y\DA
	gTmpMatrix\DB = X\DA*Y\AB + X\DB*Y\BB + X\DC*Y\CB + X\DD*Y\DB
	gTmpMatrix\DC = X\DA*Y\AC + X\DB*Y\BC + X\DC*Y\CC + X\DD*Y\DC
	gTmpMatrix\DD = X\DA*Y\AD + X\DB*Y\BD + X\DC*Y\CD + X\DD*Y\DD

	R\AA = gTmpMatrix\AA : R\AB = gTmpMatrix\AB : R\AC = gTmpMatrix\AC : R\AD = gTmpMatrix\AD
	R\BA = gTmpMatrix\BA : R\BB = gTmpMatrix\BB : R\BC = gTmpMatrix\BC : R\BD = gTmpMatrix\BD
	R\CA = gTmpMatrix\CA : R\CB = gTmpMatrix\CB : R\CC = gTmpMatrix\CC : R\CD = gTmpMatrix\CD
	R\DA = gTmpMatrix\DA : R\DB = gTmpMatrix\DB : R\DC = gTmpMatrix\DC : R\DD = gTmpMatrix\DD
End Function

Function MatrixVectorMultiply(A.TMatrix, B.TVector, R.TVector)
	gTmpVector\X = A\AA*B\X + A\AB*B\Y + A\AC*B\Z + A\AD*B\W
	gTmpVector\Y = A\BA*B\X + A\BB*B\Y + A\BC*B\Z + A\BD*B\W
	gTmpVector\Z = A\CA*B\X + A\CB*B\Y + A\CC*B\Z + A\CD*B\W
	gTmpVector\W = A\DA*B\X + A\DB*B\Y + A\DC*B\Z + A\DD*B\W

	R\X = gTmpVector\X
	R\Y = gTmpVector\Y
	R\Z = gTmpVector\Z
	R\W = gTmpVector\W
End Function

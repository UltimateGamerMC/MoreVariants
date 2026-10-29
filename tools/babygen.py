"""Generate 26.3-style baby textures for every More Mob Variants skin.

Minecraft 26.x gave babies their own models and texture layouts, so an adult skin
cannot just be drawn on a baby. For every texel of the vanilla baby model this
finds the matching spot on the adult model (same body part, same facing side,
same relative position) and copies the variant's colour from there. Texels where
vanilla's own baby art differs from its adult art (eyes, hooves, beaks moved or
redrawn) are instead recoloured from the vanilla baby through a palette learned
from vanilla adult -> variant adult, so faces stay cute and on-model.

usage: python3 -I tools/babygen.py <vanilla_assets_dir> [--sheet out.png]
"""
import math, sys, os
import numpy as np
from PIL import Image

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
TEX = os.path.join(ROOT, "src/main/resources/assets/moremobvariants/textures/entity")
PI2 = math.pi / 2


# ---------------------------------------------------------------- geometry
class Part:
	def __init__(self, offset=(0, 0, 0), rot=(0, 0, 0), parent=None):
		self.offset, self.rot, self.parent = offset, rot, parent

	def matrix(self):
		x, y, z = self.rot
		rx = np.array([[1, 0, 0], [0, math.cos(x), -math.sin(x)], [0, math.sin(x), math.cos(x)]])
		ry = np.array([[math.cos(y), 0, math.sin(y)], [0, 1, 0], [-math.sin(y), 0, math.cos(y)]])
		rz = np.array([[math.cos(z), -math.sin(z), 0], [math.sin(z), math.cos(z), 0], [0, 0, 1]])
		r, t = rz @ ry @ rx, np.array(self.offset, float)
		if self.parent is not None:
			pr, pt = self.parent.matrix()
			return pr @ r, pr @ t + pt
		return r, t


def cube(group, part, u, v, x, y, z, w, h, d, mirror=False):
	return dict(group=group, part=part, u=u, v=v, min=(x, y, z), size=(w, h, d), mirror=mirror)


def texels(cubes, tw, th):
	"""Every texel a model samples: (tx, ty, group, position, normal), mirroring ModelPart.Cube."""
	out = []
	for c in cubes:
		w, h, d = c["size"]
		x0, y0, z0 = c["min"]
		x1, y1, z1 = x0 + w, y0 + h, z0 + d
		if c["mirror"]:
			x0, x1 = x1, x0
		t0, t1, t2, t3 = (x0, y0, z0), (x1, y0, z0), (x1, y1, z0), (x0, y1, z0)
		l0, l1, l2, l3 = (x0, y0, z1), (x1, y0, z1), (x1, y1, z1), (x0, y1, z1)
		u, v = c["u"], c["v"]
		U0, U1, U2, U22, U3, U4 = u, u + d, u + d + w, u + d + w + w, u + d + w + d, u + d + w + d + w
		V0, V1, V2 = v, v + d, v + d + h
		faces = [
			((l1, l0, t0, t1), U1, V0, U2, V1, (0, -1, 0)),
			((t2, t3, l3, l2), U2, V1, U22, V0, (0, 1, 0)),
			((t0, l0, l3, t3), U0, V1, U1, V2, (-1, 0, 0)),
			((t1, t0, t3, t2), U1, V1, U2, V2, (0, 0, -1)),
			((l1, t1, t2, l2), U2, V1, U3, V2, (1, 0, 0)),
			((l0, l1, l2, l3), U3, V1, U4, V2, (0, 0, 1)),
		]
		r, t = c["part"].matrix()
		for verts, a0, b0, a1, b1, n in faces:
			if c["mirror"] and n[0] != 0:
				n = (-n[0], 0, 0)
			# vertex k gets uv: 0->(a1,b0) 1->(a0,b0) 2->(a0,b1) 3->(a1,b1)
			P = [np.array(p, float) for p in verts]
			ulo, uhi, vlo, vhi = min(a0, a1), max(a0, a1), min(b0, b1), max(b0, b1)
			for ty in range(int(vlo), int(math.ceil(vhi))):
				for tx in range(int(ulo), int(math.ceil(uhi))):
					if not (0 <= tx < tw and 0 <= ty < th):
						continue
					fu = ((tx + .5) - a0) / (a1 - a0) if a1 != a0 else .5
					fv = ((ty + .5) - b0) / (b1 - b0) if b1 != b0 else .5
					# bilinear across the quad: u runs v1->v0 (fu 0->1), v runs top->bottom
					top = P[1] + (P[0] - P[1]) * fu
					bot = P[2] + (P[3] - P[2]) * fu
					p = top + (bot - top) * fv
					out.append((tx, ty, c["group"], r @ p + t, r @ np.array(n, float)))
	return out


# ---------------------------------------------------------------- models
def quad_legs(group_size, legs, u=0, v=16, w=4, h=12, d=4, x=-2, z=-2, mirror_left=False, mirror_right=False):
	cs = []
	for name, off in legs.items():
		m = mirror_left if name.startswith("left") else mirror_right
		cs.append(cube(name, Part(off), u, v, x, 0, z, w, h, d, m))
	return cs


def adult_pig():
	head = Part((0, 12, -6))
	return [cube("head", head, 0, 0, -4, -4, -8, 8, 8, 8), cube("snout", head, 16, 16, -2, 0, -9, 4, 3, 1),
		cube("body", Part((0, 11, 2), (PI2, 0, 0)), 28, 8, -5, -10, -7, 10, 16, 8)] + quad_legs(0, {
		"right_hind_leg": (-3, 18, 7), "left_hind_leg": (3, 18, 7), "right_front_leg": (-3, 18, -5), "left_front_leg": (3, 18, -5)}, h=6, mirror_left=True)


def baby_pig():
	head = Part((0, 19, -2))
	return [cube("body", Part((0, 19, .5)), 0, 0, -3.5, -3, -4.5, 7, 6, 9),
		cube("head", head, 0, 15, -3.5, -5, -5, 7, 6, 6), cube("snout", head, 6, 27, -1.5, -1.975, -6, 3, 2, 1),
		cube("left_front_leg", Part((2.5, 22, -3)), 0, 0, -1, 0, -1, 2, 2, 2), cube("right_front_leg", Part((-2.5, 22, -3)), 23, 0, -1, 0, -1, 2, 2, 2),
		cube("left_hind_leg", Part((2.5, 22, 4)), 0, 4, -1, 0, -1, 2, 2, 2), cube("right_hind_leg", Part((-2.5, 22, 4)), 23, 4, -1, 0, -1, 2, 2, 2)]


def adult_cow_legacy():
	head = Part((0, 4, -8))
	body = Part((0, 5, 2), (PI2, 0, 0))
	return [cube("head", head, 0, 0, -4, -4, -6, 8, 8, 6), cube("horn", head, 22, 0, -5, -5, -5, 1, 3, 1), cube("horn", head, 22, 0, 4, -5, -5, 1, 3, 1),
		cube("body", body, 18, 4, -6, -10, -7, 12, 18, 10), cube("udder", body, 52, 0, -2, 2, -8, 4, 6, 1)] + quad_legs(0, {
		"right_hind_leg": (-4, 12, 7), "left_hind_leg": (4, 12, 7), "right_front_leg": (-4, 12, -5), "left_front_leg": (4, 12, -5)}, mirror_left=True)


def baby_cow():
	head = Part((0, 13.569, -5.1667))
	return [cube("head", head, 0, 18, -3, -4.569, -4.8333, 6, 6, 5), cube("horn", head, 8, 29, 3, -5.569, -3.8333, 1, 2, 1),
		cube("horn", head, 4, 29, -4, -5.569, -3.8333, 1, 2, 1, True), cube("snout", head, 12, 29, -2, -1.569, -5.8333, 4, 3, 1),
		cube("body", Part((3, 19, -5)), 0, 0, -7, -7, -1, 8, 6, 12),
		cube("right_front_leg", Part((-2.5, 18, -3.5)), 22, 18, -1.5, 0, -1.5, 3, 6, 3), cube("left_front_leg", Part((2.5, 18, -3.5)), 34, 18, -1.5, 0, -1.5, 3, 6, 3),
		cube("right_hind_leg", Part((-2.5, 18, 3.5)), 22, 27, -1.5, 0, -1.5, 3, 6, 3), cube("left_hind_leg", Part((2.5, 18, 3.5)), 34, 27, -1.5, 0, -1.5, 3, 6, 3)]


def adult_chicken():
	head = Part((0, 15, -4))
	return [cube("head", head, 0, 0, -2, -6, -2, 4, 6, 3), cube("beak", head, 14, 0, -2, -4, -4, 4, 2, 2), cube("wattle", head, 14, 4, -1, -2, -3, 2, 2, 2),
		cube("body", Part((0, 16, 0), (PI2, 0, 0)), 0, 9, -3, -4, -3, 6, 8, 6),
		cube("right_leg", Part((-2, 19, 1)), 26, 0, -1, 0, -3, 3, 5, 3), cube("left_leg", Part((1, 19, 1)), 26, 0, -1, 0, -3, 3, 5, 3),
		cube("right_wing", Part((-4, 13, 0)), 24, 13, 0, 0, -3, 1, 4, 6), cube("left_wing", Part((4, 13, 0)), 24, 13, -1, 0, -3, 1, 4, 6)]


def baby_chicken():
	body = Part((0, 20.25, -1.25))
	ll, rl = Part((1, 22, .5)), Part((-1, 22, .5))
	return [cube("chick", body, 0, 0, -2, -2.25, -.75, 4, 4, 4), cube("beak", body, 10, 8, -1, -.25, -1.75, 2, 1, 1),
		cube("left_leg", ll, 2, 2, -.5, 0, 0, 1, 2, 0), cube("left_leg", ll, 0, 1, -.5, 2, -1, 1, 0, 1),
		cube("right_leg", rl, 0, 2, -.5, 0, 0, 1, 2, 0), cube("right_leg", rl, 0, 0, -.5, 2, -1, 1, 0, 1),
		cube("right_wing", Part((2, 20, 0)), 6, 8, 0, 0, -1, 1, 0, 2), cube("left_wing", Part((-2, 20, 0)), 4, 8, -1, 0, -1, 1, 0, 2)]


def adult_cat():
	head = Part((0, 15, -9))
	return [cube("head", head, 0, 0, -2.5, -2, -3, 5, 4, 5), cube("nose", head, 0, 24, -1.5, -.001, -4, 3, 2, 2),
		cube("ear", head, 0, 10, -2, -3, 0, 1, 1, 2), cube("ear", head, 6, 10, 1, -3, 0, 1, 1, 2),
		cube("body", Part((0, 12, -10), (PI2, 0, 0)), 20, 0, -2, 3, -8, 4, 16, 6),
		cube("tail", Part((0, 15, 8), (.9, 0, 0)), 0, 15, -.5, 0, 0, 1, 8, 1), cube("tail", Part((0, 20, 14)), 4, 15, -.5, 0, 0, 1, 8, 1),
		cube("left_hind_leg", Part((1.1, 18, 5)), 8, 13, -1, 0, 1, 2, 6, 2), cube("right_hind_leg", Part((-1.1, 18, 5)), 8, 13, -1, 0, 1, 2, 6, 2),
		cube("left_front_leg", Part((1.2, 14.1, -5)), 40, 0, -1, 0, 0, 2, 10, 2), cube("right_front_leg", Part((-1.2, 14.1, -5)), 40, 0, -1, 0, 0, 2, 10, 2)]


def baby_cat():
	head = Part((0, 20, -3.125))
	return [cube("head", head, 0, 0, -2.5, -3, -2.875, 5, 4, 4), cube("ear", head, 18, 0, -2, -4, -.875, 1, 1, 2),
		cube("ear", head, 24, 0, 1, -4, -.875, 1, 1, 2), cube("nose", head, 18, 3, -1.5, -1, -3.875, 3, 2, 1),
		cube("left_front_leg", Part((1, 22, -1.5)), 18, 18, -.5, 0, -1, 1, 2, 2), cube("right_front_leg", Part((-1, 22, -1.5)), 12, 18, -.5, 0, -1, 1, 2, 2),
		cube("left_hind_leg", Part((1, 22, 2.5)), 18, 22, -.5, 0, -1, 1, 2, 2), cube("right_hind_leg", Part((-1, 22, 2.5)), 12, 22, -.5, 0, -1, 1, 2, 2),
		cube("body", Part((0, 20.5, .5)), 0, 8, -2, -1.5, -3.5, 4, 3, 7),
		cube("tail", Part((0, 19.107, 3.9151), (-.567232, 0, 0)), 0, 18, -.5, -.107, .0849, 1, 1, 5)]


def adult_sheep(fur=False):
	head, body = Part((0, 6, -8)), Part((0, 5, 2), (PI2, 0, 0))
	legs = {"right_hind_leg": (-3, 12, 7), "left_hind_leg": (3, 12, 7), "right_front_leg": (-3, 12, -5), "left_front_leg": (3, 12, -5)}
	if fur:
		return [cube("head", head, 0, 0, -3, -4, -4, 6, 6, 6), cube("body", body, 28, 8, -4, -10, -7, 8, 16, 6)] + quad_legs(0, legs, h=6)
	return [cube("head", head, 0, 0, -3, -4, -6, 6, 6, 8), cube("body", body, 28, 8, -4, -10, -7, 8, 16, 6)] + quad_legs(0, legs, mirror_right=True)


def baby_sheep():
	return [cube("body", Part((0, 17, .5)), 0, 10, -3, -2, -4.5, 6, 4, 9), cube("head", Part((0, 15.5, -2.5)), 0, 0, -2.5, -4.5, -3.5, 5, 5, 5),
		cube("right_hind_leg", Part((-2, 19, 3)), 0, 23, -1, 0, -1, 2, 5, 2), cube("left_hind_leg", Part((2, 19, 3)), 24, 12, -1, 0, -1, 2, 5, 2),
		cube("right_front_leg", Part((-2, 19, -2)), 8, 23, -1, 0, -1, 2, 5, 2), cube("left_front_leg", Part((2, 19, -2)), 24, 5, -1, 0, -1, 2, 5, 2)]


def adult_wolf():
	head = Part((0, 0, 0), parent=Part((-1, 13.5, -7)))
	tail = Part((0, 0, 0), parent=Part((-1, 12, 8), (math.pi / 5, 0, 0)))
	return [cube("head", head, 0, 0, -2, -3, -2, 6, 6, 4), cube("ear", head, 16, 14, -2, -5, 0, 2, 2, 1), cube("ear", head, 16, 14, 2, -5, 0, 2, 2, 1),
		cube("snout", head, 0, 10, -.5, -.001, -5, 3, 3, 4),
		cube("body", Part((0, 14, 2), (PI2, 0, 0)), 18, 14, -3, -2, -3, 6, 9, 6), cube("body", Part((-1, 14, -3), (PI2, 0, 0)), 21, 0, -3, -3, -3, 8, 6, 7),
		cube("tail", tail, 9, 18, 0, 0, -1, 2, 8, 2)] + quad_legs(0, {
		"right_hind_leg": (-2.5, 16, 7), "left_hind_leg": (.5, 16, 7), "right_front_leg": (-2.5, 16, -4), "left_front_leg": (.5, 16, -4)},
		u=0, v=18, w=2, h=8, d=2, x=0, z=-1, mirror_right=True)


def baby_wolf():
	head = Part((0, 18.25, -4))
	tail = Part((0, -.6, .2), (-3.1, 0, 0), parent=Part((0, 19, 3), (-.5236, 0, 0)))
	return [cube("head", head, 0, 12, -2.99, -3.25, -3, 6, 5, 5), cube("snout", head, 17, 12, -1.5, -.24, -5, 3, 2, 2),
		cube("ear", Part((-2, -4.25, -.5), parent=head), 0, 5, -1, -1, -.5, 2, 2, 1), cube("ear", Part((2, -4.25, -.5), parent=head), 20, 5, -1, -1, -.5, 2, 2, 1),
		cube("body", Part((0, 19, 0)), 0, 0, -3, -2, -4, 6, 4, 8),
		cube("right_hind_leg", Part((-1.5, 21, 3)), 0, 22, -1, 0, -1, 2, 3, 2), cube("left_hind_leg", Part((1.5, 21, 3)), 8, 22, -1, 0, -1, 2, 3, 2),
		cube("right_front_leg", Part((-1.5, 21, -3)), 0, 0, -1, 0, -1, 2, 3, 2), cube("left_front_leg", Part((1.5, 21, -3)), 20, 0, -1, 0, -1, 2, 3, 2),
		cube("tail", tail, 22, 16, -1, -5.7, -1, 2, 6, 2)]


def adult_zombie():
	head = Part((0, 0, 0))
	return [cube("head", head, 0, 0, -4, -8, -4, 8, 8, 8), cube("hat", head, 32, 0, -4, -8, -4, 8, 8, 8),
		cube("body", Part((0, 0, 0)), 16, 16, -4, 0, -2, 8, 12, 4),
		cube("right_arm", Part((-5, 2, 0)), 40, 16, -3, -2, -2, 4, 12, 4), cube("left_arm", Part((5, 2, 0)), 40, 16, -1, -2, -2, 4, 12, 4, True),
		cube("right_leg", Part((-1.9, 12, 0)), 0, 16, -2, 0, -2, 4, 12, 4), cube("left_leg", Part((1.9, 12, 0)), 0, 16, -2, 0, -2, 4, 12, 4, True)]


def baby_zombie():
	head = Part((0, 15.25, 0))
	return [cube("body", Part((0, 17.5, 0)), 16, 16, -2, -2.5, -1, 4, 5, 2),
		cube("head", head, 3, 3, -3, -6.25, -3, 6, 6, 6), cube("hat", head, 35, 3, -3, -6.15, -3, 6, 6, 6),
		cube("right_arm", Part((-3, 15.5, 0)), 36, 16, -1, -.5, -1, 2, 5, 2), cube("left_arm", Part((3, 15.5, 0)), 28, 16, -1, -.5, -1, 2, 5, 2),
		cube("right_leg", Part((-1, 20, 0)), 8, 16, -1, 0, -1, 2, 4, 2), cube("left_leg", Part((1, 20, 0)), 0, 16, -1, 0, -1, 2, 4, 2)]


# baby group -> adult groups it is sampled from
# (used when the adult model has no part of the same name)
LINKS = {"chick": ["head", "body"], "snout": ["head"], "nose": ["head"]}


# ---------------------------------------------------------------- mapping
def build_map(adult, asize, baby, bsize):
	"""For each baby texel, the adult texel it should copy from: {(bx,by): (ax,ay)}."""
	A = texels(adult, *asize)
	B = texels(baby, *bsize)
	groups = {}
	for t in A:
		groups.setdefault(t[2], []).append(t)
	def bbox(ts):
		p = np.array([t[3] for t in ts])
		lo, hi = p.min(0), p.max(0)
		return lo, np.where(hi - lo < 1e-6, 1, hi - lo)
	bgroups = {}
	for t in B:
		bgroups.setdefault(t[2], []).append(t)
	mapping = {}
	for g, bts in bgroups.items():
		src = [a for name in ([g] if g in groups else LINKS.get(g, [])) for a in groups.get(name, [])]
		if not src:
			raise SystemExit(f"no adult group for baby {g}")
		alo, aspan = bbox(src)
		blo, bspan = bbox(bts)
		ap = (np.array([a[3] for a in src]) - alo) / aspan
		an = np.array([a[4] for a in src])
		for bx, by, _, p, n in bts:
			q = (p - blo) / bspan
			dist = np.linalg.norm(ap - q, axis=1) + 2.0 * (an @ n < .5)
			a = src[int(dist.argmin())]
			mapping.setdefault((bx, by), (a[0], a[1]))
	return mapping


def load(path):
	return np.array(Image.open(path).convert("RGBA")).astype(int)


def resample(img, mapping, shape):
	out = np.zeros(shape + (4,), int)
	for (bx, by), (ax, ay) in mapping.items():
		if ay < img.shape[0] and ax < img.shape[1]:
			out[by, bx] = img[ay, ax]
	return out


def palette(v_adult, m_adult):
	"""vanilla adult colour -> the variant's most common colour at those same texels."""
	votes = {}
	h, w = min(v_adult.shape[0], m_adult.shape[0]), min(v_adult.shape[1], m_adult.shape[1])
	for y in range(h):
		for x in range(w):
			a, b = v_adult[y, x], m_adult[y, x]
			if a[3] < 128 or b[3] < 128:
				continue
			votes.setdefault(tuple(a[:3]), {}).setdefault(tuple(b), 0)
			votes[tuple(a[:3])][tuple(b)] += 1
	keys = np.array(list(votes.keys()))
	vals = [max(v.items(), key=lambda kv: kv[1])[0] for v in votes.values()]
	return keys, vals


def make_baby(mapping, v_adult, v_baby, m_adult, keep_alpha_from_variant=False):
	shape = v_baby.shape[:2]
	pattern = resample(m_adult, mapping, shape)
	if keep_alpha_from_variant:
		return pattern
	expected = resample(v_adult, mapping, shape)
	keys, vals = palette(v_adult, m_adult)
	out = v_baby.copy()
	for (bx, by) in mapping:
		vb = v_baby[by, bx]
		if vb[3] == 0:
			continue
		if np.abs(vb[:3] - expected[by, bx][:3]).sum() < 90 and pattern[by, bx][3] > 0:
			px = pattern[by, bx].copy()
			# keep vanilla baby shading: shift brightness by how much the baby art differs from the adult art
			px[:3] = np.clip(px[:3] + (vb[:3] - expected[by, bx][:3]), 0, 255)
		else:
			px = np.array(vals[int(np.abs(keys - vb[:3]).sum(1).argmin())])
		px[3] = vb[3]
		out[by, bx] = px
	return out


CHICK_FLUFF = [(255, 220, 50), (255, 192, 15), (237, 158, 12)]


def make_chick(mapping, v_adult, v_baby, m_adult, feathers):
	"""Chicks are fluffballs, not tiny chickens: keep vanilla's chick art and tint its
	three fluff tones with the variant's light/mid/dark feather colours (as vanilla does
	for cold and warm chicks); feet, beak and eyes go through the palette."""
	out = make_baby(mapping, v_adult, v_baby, m_adult)
	# dominant feather colour, bucketed so dithered shades of one colour vote together
	votes = {}
	for x, y in feathers:
		c = m_adult[y, x]
		if c[3] > 0:
			votes.setdefault(tuple(c[:3] // 24), []).append(c[:3])
	base = np.mean(max(votes.values(), key=len), axis=0)
	ramp = [np.append(np.clip(base * k, 0, 255), 255).astype(int) for k in (1.12, 1.0, .86)]
	for y in range(v_baby.shape[0]):
		for x in range(v_baby.shape[1]):
			rgb = tuple(v_baby[y, x][:3])
			if v_baby[y, x][3] and rgb in CHICK_FLUFF:
				out[y, x] = ramp[CHICK_FLUFF.index(rgb)]
			elif v_baby[y, x][3] and rgb == (0, 0, 0):
				out[y, x] = v_baby[y, x]
	return out


def closest(m_adult, candidates):
	def score(path):
		v = load(path)
		h, w = min(v.shape[0], m_adult.shape[0]), min(v.shape[1], m_adult.shape[1])
		return np.abs(v[:h, :w, :3] - m_adult[:h, :w, :3]).sum()
	return min(candidates, key=score)


# ---------------------------------------------------------------- jobs
def main():
	van = os.path.join(sys.argv[1], "assets/minecraft/textures/entity")
	V = lambda p: os.path.join(van, p)
	sheet = []
	m = {
		"pig": build_map(adult_pig(), (64, 64), baby_pig(), (32, 32)),
		"cow": build_map(adult_cow_legacy(), (64, 32), baby_cow(), (64, 64)),
		"chicken": build_map(adult_chicken(), (64, 32), baby_chicken(), (16, 16)),
		"cat": build_map(adult_cat(), (64, 32), baby_cat(), (32, 32)),
		"sheep": build_map(adult_sheep(), (64, 32), baby_sheep(), (64, 32)),
		"wool": build_map(adult_sheep(True), (64, 32), baby_sheep(), (64, 32)),
		"wolf": build_map(adult_wolf(), (64, 32), baby_wolf(), (32, 32)),
		"zombie": build_map(adult_zombie(), (64, 64), baby_zombie(), (64, 64)),
	}
	feathers = {(t[0], t[1]) for t in texels(adult_chicken(), 64, 32) if t[2] in ("head", "body", "right_wing", "left_wing")}
	legacy_cow = os.path.join(TEX, "cow/legacy_reference.png")
	refs = {
		"pig": lambda f: (V("pig/pig_temperate.png"), V("pig/pig_temperate_baby.png")),
		"cow": lambda f: (legacy_cow, V("cow/cow_temperate_baby.png")),
		"chicken": lambda f: (V("chicken/chicken_temperate.png"), V("chicken/chicken_temperate_baby.png")),
		"sheep": lambda f: (V("sheep/sheep.png"), V("sheep/sheep_baby.png")),
		"zombie": lambda f: (V("zombie/zombie.png"), V("zombie/zombie_baby.png")),
	}
	for mob in ["pig", "cow", "chicken", "cat", "sheep", "wolf", "zombie"]:
		d = os.path.join(TEX, mob)
		for f in sorted(os.listdir(d)):
			if not f.endswith(".png") or f.endswith("_baby.png") or f == "legacy_reference.png":
				continue
			m_adult = load(os.path.join(d, f))
			if mob == "cat":
				adults = [V("cat/" + c) for c in os.listdir(V("cat")) if c.startswith("cat_") and not c.endswith("_baby.png") and "collar" not in c]
				va = closest(m_adult, adults)
				vb = va[:-4] + "_baby.png"
			elif mob == "wolf":
				state = f[:-4].rsplit("_", 1)[1]
				suffix = {"wild": "", "tame": "_tame", "angry": "_angry"}[state]
				adults = [V("wolf/" + c) for c in os.listdir(V("wolf")) if c.endswith(suffix + ".png") and "baby" not in c and "collar" not in c and "armor" not in c
					and (suffix or not (c.endswith("_tame.png") or c.endswith("_angry.png")))]
				va = closest(m_adult, adults)
				vb = va[:-4] + "_baby.png"
			else:
				va, vb = refs[mob](f)
			if mob == "chicken":
				baby = make_chick(m[mob], load(va), load(vb), m_adult, feathers)
			else:
				baby = make_baby(m[mob], load(va), load(vb), m_adult)
			out = os.path.join(d, f[:-4] + "_baby.png")
			Image.fromarray(baby.astype(np.uint8)).save(out)
			sheet.append((mob + "/" + f, m_adult, baby, load(vb)))
	# overlays drawn with the baby body/wool models
	for sub, mp in [("sheep/wool", "wool"), ("sheep/sheared_color_overlay", "sheep"), ("pig/mud", "pig")]:
		d = os.path.join(TEX, sub)
		shape = (64, 32) if mp != "pig" else (32, 32)
		for f in sorted(os.listdir(d)):
			if not f.endswith(".png") or f.endswith("_baby.png"):
				continue
			m_adult = load(os.path.join(d, f))
			if mp == "wool":
				baby = make_baby(m[mp], load(V("sheep/sheep_wool.png")), load(V("sheep/sheep_wool_baby.png")), m_adult)
			else:
				baby = resample(m_adult, m[mp], (shape[1], shape[0]))
			Image.fromarray(baby.astype(np.uint8)).save(os.path.join(d, f[:-4] + "_baby.png"))
			sheet.append((sub + "/" + f, m_adult, baby, baby))
	# self-check: vanilla adult remapped should look like vanilla baby
	for mob, (va, vb) in [("pig", refs["pig"](0)), ("chicken", refs["chicken"](0)), ("sheep", refs["sheep"](0)), ("zombie", refs["zombie"](0)),
			("cat", (V("cat/cat_tabby.png"), V("cat/cat_tabby_baby.png"))), ("wolf", (V("wolf/wolf.png"), V("wolf/wolf_baby.png"))), ("cow", (legacy_cow, V("cow/cow_temperate_baby.png")))]:
		b = load(vb)
		sheet.append(("CHECK " + mob, load(va), resample(load(va), m[mob], b.shape[:2]), b))
	if "--sheet" in sys.argv:
		write_sheet(sheet, sys.argv[sys.argv.index("--sheet") + 1])
	if "--grid" in sys.argv:
		write_grid(sheet, sys.argv[sys.argv.index("--grid") + 1])


def write_sheet(rows, path):
	from PIL import ImageDraw
	S, cell = 4, 260
	img = Image.new("RGBA", (cell * 3 + 220, cell * len(rows)), (60, 60, 70, 255))
	dr = ImageDraw.Draw(img)
	for i, (name, a, b, c) in enumerate(rows):
		dr.text((4, i * cell + 4), name, fill=(255, 255, 255, 255))
		for j, arr in enumerate([a, b, c]):
			im = Image.fromarray(arr.astype(np.uint8))
			scale = min((cell - 10) // im.width, (cell - 10) // im.height, S * 2)
			im = im.resize((im.width * scale, im.height * scale), Image.NEAREST)
			img.alpha_composite(im, (220 + j * cell, i * cell + 5))
	img.save(path)


def write_grid(rows, path):
	"""Generated babies side by side, one tile each, for eyeballing."""
	from PIL import ImageDraw
	rows = [r for r in rows if not r[0].startswith("CHECK")]
	cols, tw, th = 6, 300, 300
	img = Image.new("RGBA", (cols * tw, th * ((len(rows) + cols - 1) // cols)), (60, 60, 70, 255))
	dr = ImageDraw.Draw(img)
	for i, (name, _, b, _) in enumerate(rows):
		x, y = (i % cols) * tw, (i // cols) * th
		im = Image.fromarray(b.astype(np.uint8))
		k = max(1, min((tw - 10) // im.width, (th - 24) // im.height))
		img.alpha_composite(im.resize((im.width * k, im.height * k), Image.NEAREST), (x + 5, y + 18))
		dr.text((x + 5, y + 3), name, fill=(255, 255, 255, 255))
	img.save(path)


if __name__ == "__main__":
	main()

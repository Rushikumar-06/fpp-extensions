package me.bill.fppswap;

enum SwapPersonality {
  CASUAL(1.0),
  GRINDER(1.6),
  SOCIAL(0.65),
  LURKER(2.2),
  ACTIVE(0.45),
  SPORADIC(1.1);

  final double sessionMultiplier;

  SwapPersonality(double multiplier) {
    this.sessionMultiplier = multiplier;
  }
}

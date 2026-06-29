package com.hask.shop.custom.booster;

import com.gmail.nossr50.datatypes.skills.SkillType;

public class BoosterSession {

    public final SkillType skill;
    public final double multiplier;
    public int remainingTicks;

    public BoosterSession(SkillType skill, double multiplier, int durationTicks) {
        this.skill = skill;
        this.multiplier = multiplier;
        this.remainingTicks = durationTicks;
    }

}

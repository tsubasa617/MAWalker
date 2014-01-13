package walker;

import info.Card;
import info.FairyBattleInfo;

import java.util.List;

import action.ActionRegistry;
import action.ActionRegistry.Action;

public class Think {

	private static final String AP_HALF = "101";
	private static final String BC_HALF = "111";
	private static final String AP_FULL = "1";
	private static final String BC_FULL = "2";

	private static final int EXPLORE_NORMAL = 60;
	private static final int EXPLORE_URGENT = 80;
	private static final int GFL_PRI = 50;
	private static final int GFL_HI_PRI = 70;
	private static final int GF_PRI = 25;
	private static final int USE_PRI = 99;

	public static ActionRegistry.Action doIt(
			List<ActionRegistry.Action> possible) {
		Action best = Action.NOTHING;
		int score = Integer.MIN_VALUE + 20;
		for (int i = 0; i < possible.size(); i++) {
			switch (possible.get(i)) {
			case LOGIN:
				return ActionRegistry.Action.LOGIN;
			case ADD_AREA:
				return Action.ADD_AREA;
			case GET_FLOOR_INFO:
				return Action.GET_FLOOR_INFO;
			case GET_FAIRY_LIST:
				if (Info.FairyBattleFirst) {
					if (score < GFL_HI_PRI) {
						best = Action.GET_FAIRY_LIST;
						score = GFL_HI_PRI;
					}
				} else {
					if (score < GFL_PRI) {
						best = Action.GET_FAIRY_LIST;
						score = GFL_PRI;
					}
				}
				break;
			case GOTO_FLOOR:
				if (score < GF_PRI) {
					best = Action.GOTO_FLOOR;
					score = GF_PRI;
				}
				break;
			case PRIVATE_FAIRY_BATTLE:
				if (Think.canBattle())
					return Action.PRIVATE_FAIRY_BATTLE;
				break;
			case EXPLORE:
				int p = explorePoint();
				if (p > score) {
					best = Action.EXPLORE;
					score = p;
				}
				break;
			case GUILD_BATTLE:
				Process.info.gfairy.No = Info.PublicFairyBattle.No;
				return Action.GUILD_BATTLE;
			case GUILD_TOP:
				return Action.GUILD_TOP;
			case GET_FAIRY_REWARD:
				return Action.GET_FAIRY_REWARD;
			case PFB_GOOD:
				return Action.PFB_GOOD;
			case RECV_PFB_GOOD:
				return Action.RECV_PFB_GOOD;
			case NOTHING:
				break;
			case SELL_CARD:
				if (Info.autoSellCard == true && cardsToSell() == true) {
					return Action.SELL_CARD;
				}
				break;
			case LV_UP:
				decideUpPoint();
				return Action.LV_UP;
			case USE:
				int ptr = decideUse();
				if (ptr > score) {
					best = Action.USE;
					score = ptr;
				}
				break;
			default:
				break;
			}
		}
		return best;
	}

	private static int decideUse() {

		if (Info.autoUseAp) {
			if (Process.info.ap < Info.autoApLow) {
				switch (Info.autoApType) {
				case ALL:
					if (Process.info.halfApToday > 0 && Process.info.halfAp > 0) {
						Process.info.toUse = AP_HALF;
						return USE_PRI;
					} else {
						if (Process.info.fullAp > Info.autoApFullLow) {
							Process.info.toUse = AP_FULL;
							return USE_PRI;
						}
					}
					break;
				case FULL_ONLY:
					if (Process.info.fullAp > Info.autoApFullLow) {
						Process.info.toUse = AP_FULL;
						return USE_PRI;
					}
					break;
				case HALF_ONLY:
					if (Process.info.halfApToday > 0 && Process.info.halfAp > 0) {
						Process.info.toUse = AP_HALF;
						return USE_PRI;
					}
					break;
				default:
					break;

				}
			}
		}
		if (Info.autoUseBc) {
			if (Process.info.bc < Info.autoBcLow) {
				switch (Info.autoBcType) {
				case ALL:
					if (Process.info.halfBcToday > 0 && Process.info.halfBc > 0) {
						Process.info.toUse = BC_HALF;
						return USE_PRI;
					} else {
						if (Process.info.fullBc > Info.autoBcFullLow) {
							Process.info.toUse = BC_FULL;
							return USE_PRI;
						}
					}
					break;
				case FULL_ONLY:
					if (Process.info.fullBc > Info.autoBcFullLow) {
						Process.info.toUse = BC_FULL;
						return USE_PRI;
					}
					break;
				case HALF_ONLY:
					if (Process.info.halfBcToday > 0 && Process.info.halfBc > 0) {
						Process.info.toUse = BC_HALF;
						return USE_PRI;
					}
					break;
				default:
					break;

				}
			}
		}
		return Integer.MIN_VALUE;
	}

	private static boolean canBattle() {
		switch (Process.info.fairy.Type) {
		case 0:
			break;
		case FairyBattleInfo.PRIVATE:
		case FairyBattleInfo.PRIVATE | FairyBattleInfo.RARE:
			if (Process.info.bc < Info.FriendFairyBattleNormal.BC
					|| Process.info.bc < 2) {
				return false;
			}

			break;
		case FairyBattleInfo.PRIVATE | FairyBattleInfo.SELF:
		case FairyBattleInfo.PRIVATE | FairyBattleInfo.SELF
				| FairyBattleInfo.RARE:
			if (Process.info.bc < Info.PrivateFairyBattleNormal.BC
					|| Process.info.bc < 2) {
				return false;
			}

			break;
		default:
			return false;
		}

		return true;
	}

	private static void decideUpPoint() {
		if (Info.AutoAddp == 1) {
			// 主号全加BC
			Process.info.apUp = 0;
			Process.info.bcUp = Process.info.pointToAdd;
		} else if (Info.AutoAddp == 2) {
			// 小号全加AP
			Process.info.apUp = Process.info.pointToAdd;
			Process.info.bcUp = 0;
		}
	}

	private static int explorePoint() {
		try {
			// 判断是否可以行动
			if (Process.info.front == null)
				Process.info.front = Process.info.floor.firstEntry().getValue();
			if (!Info.AllowBCInsuffient
					&& Process.info.bc < Info.PrivateFairyBattleNormal.BC)
				return Integer.MIN_VALUE;
			if (Process.info.ap < Process.info.front.cost)
				return Integer.MIN_VALUE;
			if (Process.info.ap == Process.info.apMax)
				return EXPLORE_URGENT;
		} catch (Exception ex) {
			ex.printStackTrace();
			return Integer.MIN_VALUE;
		}
		return EXPLORE_NORMAL;
	}

	public static boolean cardsToSell() {
		int count = 0;
		String toSell = "";

		for (Card c : Process.info.cardList) {
			if (!c.exist) {
				continue;
			}

			// 闪卡不卖，但是低等级的闪卡照样要卖
			if (c.holo && c.price >= 3000) {
				continue;
			}

			// 防止不小心把贵重卡片卖了
			if (c.hp > 6000) {
				continue;
			}

			// 各种切利
			if (c.hp <= 2 && c.atk <= 2 && !Info.CanBeSold.contains(c.cardId)) {
				continue;
			}

			// 狼娘 牛仔 女仆 不卖（若用户不特别指定）
			if ((c.cardId.equals(124) || c.cardId.equals(142) || c.cardId
					.equals(9)) && !Info.CanBeSold.contains(c.cardId)) {
				continue;
			}

			if (c.price <= 3000 || Info.CanBeSold.contains(c.cardId)) {
				if (toSell.isEmpty()) {
					toSell = c.serialId;
				} else {
					toSell += "," + c.serialId;
				}
				count++;
				c.exist = false;
			}

			if (count >= 30) {
				break;
			}
		}

		Process.info.toSell = toSell;
		return !toSell.isEmpty();
	}

}

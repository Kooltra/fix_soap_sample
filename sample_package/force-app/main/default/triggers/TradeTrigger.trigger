trigger TradeTrigger on Kooltra__FxTrade__c (after insert, after update) {
	if (trigger.isInsert) {
		List<TradeInserted__e> events = new List<TradeInserted__e>();
		for (Kooltra__FxTrade__c trade : trigger.new) {
			events.add(new TradeInserted__e(TradeId__c = trade.id));
		}
		EventBus.publish(events);
	}
}
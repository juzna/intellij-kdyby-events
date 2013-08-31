<?php

class OrderLogger implements \Kdyby\Events\Subscriber
{

    public function getSubscribedEvents()
    {
        return array(
            'OrderService::onAdd' => 'logAdd',
            'OrderService::onSave' => 'logAdd',
        );
    }

    public function logAdd()
    {

    }

}

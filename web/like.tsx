import * as React from 'react'
var $: any;

/** App has two components: number of likes and id number */
type LikeProps = {
    mId: number,
    mLikes: number
}

/** The Like component allows the user to interact with local state */
export class Like extends React.Component<LikeProps> {
    /**
     * The default state constructor for Like.
     */
    state = {
        mId: 0,
        mLikes: 0
    }

    /**
     * increment the number that is stored in the state
     * then POST to /messages/id/like everytime the button is pressed
     */
    increment = (_e: React.MouseEvent<HTMLButtonElement>) => {
        // NB: setState will patch the state by updating any fields that are
        //     defined in the object that it is given.
        this.setState({ num: ++this.state.mLikes });
        $.ajax({
            type: "POST",
            url: "/messages/" + this.state.mId + '/like',
            //success: mainList.update
        });
    }

    /**
     * Render the component.
     */
    render() {
        return (
            <span>
                <p>{this.state.mLikes}</p>
                <button onClick={this.increment}> Like</button>
            </span>
        );
    }
};